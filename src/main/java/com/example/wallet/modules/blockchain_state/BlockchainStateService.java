package com.example.wallet.modules.blockchain_state;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import com.example.wallet.config.Web3jConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Service
public class BlockchainStateService {
  private static final Logger logger = LogManager.getLogger(BlockchainStateService.class);

  private static final BigInteger CONFIRMATION_BLOCKS = BigInteger.valueOf(12);

  private final Web3jConfig web3jConfig;
  private final BlockchainStateRepository blockchainStateRepository;

  @Autowired
  public BlockchainStateService(Web3jConfig web3jConfig, BlockchainStateRepository blockchainStateRepository) {
    this.web3jConfig = web3jConfig;
    this.blockchainStateRepository = blockchainStateRepository;
  }

  public BlockchainState findOne(String networkName) throws RuntimeException {
    try {
      BlockchainState blockchainState = blockchainStateRepository.findByNetworkName(networkName);
      if (blockchainState == null) {
        throw new RuntimeException("BlockchainState not found");
      }
      return blockchainState;
    } catch (Exception e) {
      logger.debug("Failed to find blockchain state for network: " + networkName);
      return null;
    }
  }

  public BigInteger getNextBlockNumner(String networkName) throws RuntimeException {
    try {
      BigInteger latestBlockNumber = getLatestBlockNumber(networkName);
      BigInteger nextBlockNumber = latestBlockNumber.subtract(CONFIRMATION_BLOCKS);
      BlockchainState blockchainState = blockchainStateRepository.findByNetworkName(networkName);
      if (blockchainState == null) {
        blockchainState = new BlockchainState(networkName, nextBlockNumber);
        blockchainStateRepository.save(blockchainState);
      } else if (blockchainState.getNextBlockNumber() != BigInteger.ONE) {
        nextBlockNumber = blockchainState.getNextBlockNumber();
      } 
      return nextBlockNumber;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void updateNextBlockNumber(String networkName, BigInteger lastProcessedBlockNumber) throws RuntimeException {
    try {
      BlockchainState blockchainState = blockchainStateRepository.findByNetworkName(networkName);
      BigInteger nextBlockNumber = lastProcessedBlockNumber.add(BigInteger.ONE);
      if (blockchainState == null) {
        blockchainState = new BlockchainState(networkName, nextBlockNumber);
      } 
      blockchainStateRepository.save(blockchainState);
      logger.debug("Next block number of blockchain network [{}] updated : {}", networkName, nextBlockNumber);
    } catch (Exception e) {
      logger.error("Failed to update next block number.");
      e.printStackTrace();
    }
  }

  public BlockchainState save(BlockchainState blockchainState) throws RuntimeException {
    try {
      return blockchainStateRepository.save(blockchainState);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public BigInteger getLatestBlockNumber(String networkName) {
    try {
      Web3j web3j = web3jConfig.getWeb3jInstanceMap().get(networkName);
      return web3j.ethBlockNumber().send().getBlockNumber();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }  
}
