package com.example.wallet.modules.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import com.example.wallet.config.Web3jConfig;
import com.example.wallet.modules.blockchain_state.BlockchainStateService;
import com.example.wallet.modules.event.Event.TransactionStatus;

import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class EventListener {
  private static final Logger logger = LogManager.getLogger(EventListener.class);

  private static final BigInteger CONFIRMATION_BLOCKS = BigInteger.valueOf(12);

  private final Web3jConfig web3jConfig;
  private final ExecutorService executorService;
  private final EventService eventService;
  private final BlockchainStateService blockchainStateService;

  private Disposable subscription;

  @Autowired
  public EventListener(Web3jConfig web3jConfig, EventService eventService, BlockchainStateService blockchainStateService) {
    this.web3jConfig = web3jConfig;
    this.eventService = eventService;
    this.blockchainStateService = blockchainStateService;
    executorService = Executors.newFixedThreadPool(web3jConfig.getWeb3jInstanceMap().size() * 2);
  }

  @PostConstruct
  public void start() {
    Map<String, Web3j> web3jInstanceMap = web3jConfig.getWeb3jInstanceMap();
    for (String networkName : web3jInstanceMap.keySet()) {
        logger.info("Starting event listener for network: [{}]", networkName);
        executorService.submit(() -> runListener(networkName));
    }
  }

  @PreDestroy
  public void stop() {
    logger.info("Stopping event listener...");
    executorService.shutdown();
    if (subscription != null) {
      subscription.dispose();
    }

    Map<String, Web3j> web3jInstanceMap = web3jConfig.getWeb3jInstanceMap();
    for (String networkName : web3jInstanceMap.keySet()) {
        Web3j web3j = web3jInstanceMap.get(networkName);
        web3j.shutdown();
    }
  }

  private void runListener(String networkName) {
    Web3j web3j = web3jConfig.getWeb3jInstanceMap().get(networkName);

    // TODO : Replay past blocks for server restart
    // BigInteger startBlockNumber = blockchainStateService.getNextBlockNumner(networkName);
    // BigInteger currentBlockNumber = blockchainStateService.getLatestBlockNumber(networkName);
    // subscription = web3j.replayPastAndFutureBlocksFlowable(DefaultBlockParameter.valueOf(startBlockNumber), false)
    //     .mergeWith(web3j.blockFlowable(false).filter(ethBlock -> ethBlock.getBlock().getNumber().compareTo(currentBlockNumber) > 0))
    //     .subscribe(ethBlock -> onNext(networkName, ethBlock), this::onError);

    subscription = web3j.blockFlowable(false).subscribe(ethBlock -> onNext(networkName, ethBlock), this::onError);
  }

  private void onNext(String networkName, EthBlock ethBlock) {
    try {
      BigInteger latestBlockNumber = blockchainStateService.getLatestBlockNumber(networkName);
      BigInteger blockNumber = ethBlock.getBlock().getNumber();
      logger.info("Latest block number: {} | Incomming commit block event: {}", latestBlockNumber, blockNumber);
      
      // Process new block
      processNewBlock(networkName, ethBlock, blockNumber);

      // Process mined blocks
      processMinedBlocks(networkName, blockNumber);

      // Update blockchain state
      blockchainStateService.updateNextBlockNumber(networkName, blockNumber);

    } catch (Exception e) {
      e.printStackTrace();
    } 
  }

  private void processNewBlock(String networkName, EthBlock ethBlock, BigInteger blockNumber) throws IOException {
    logger.info("Processing mined block: {}", blockNumber);
    for (EthBlock.TransactionResult txResult : ethBlock.getBlock().getTransactions()) {
      String txHash = (String) txResult.get();
      EthTransaction ethTransaction;
      try {
        Web3j web3j = web3jConfig.getWeb3jInstanceMap().get(networkName);
        ethTransaction = web3j.ethGetTransactionByHash(txHash).send();
      } catch (IOException e) {
        logger.error("Error retrieving transaction: {}", txHash);
        e.printStackTrace();
        continue;
      }

      Transaction tx = ethTransaction.getTransaction().orElse(null);
      if (tx == null) continue;
      
      // Check if the transaction is outgoing
      if (tx.getTo() != null) {
        eventService.createMinedEvent(tx, blockNumber);
      }
    }
  }

  private void processMinedBlocks(String networkName, BigInteger blockNumber) {
    logger.info("Processing confirmed block: {}", blockNumber);
    List<Event> unconfirmedEvents = eventService.findLessThan12Confirmations(networkName, TransactionStatus.MINED);
    for (Event event : unconfirmedEvents) {
      BigInteger blockConfirmations = blockNumber.subtract(event.getBlockNumber());

      if (blockConfirmations.compareTo(CONFIRMATION_BLOCKS) >= 0) {
        eventService.createConfirmedEvent(event);
        event.setTxStatus(TransactionStatus.CONFIRMED);
      }
      event.setBlockConfirmations(blockConfirmations);
      eventService.save(event);
    }
  }

  private void onError(Throwable throwable) {
    logger.error("Error in event listener: {}", throwable.getMessage());
    throwable.printStackTrace();
    restartListener();
  }

  private void restartListener() {
    try {
      logger.info("Restarting event listener...");
      Thread.sleep(5000); // wait for 5 seconds before restarting
      start();
    } catch (InterruptedException e) {
      logger.error("Thread was interrupted, stopping event listener...");
      Thread.currentThread().interrupt();
    }
  }
}
