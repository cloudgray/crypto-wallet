package com.example.wallet.modules.balance;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;

import com.example.wallet.config.Web3jConfig;
import com.example.wallet.modules.blockchain_state.BlockchainStateService;
import com.example.wallet.modules.event.Event;
import com.example.wallet.modules.event.EventService;

@Service
public class BalanceService {
  private final Web3jConfig web3jConfig;
  private final BlockchainStateService blockchainStateService;
  private final BalanceRepository balanceRepository;
  private final EventService eventService;

  @Autowired
  public BalanceService(
    Web3jConfig web3jConfig,
    BalanceRepository balanceRepository, 
    EventService eventService,
    BlockchainStateService blockchainStateService
  ) {
    this.web3jConfig = web3jConfig;
    this.balanceRepository = balanceRepository;
    this.eventService = eventService;
    this.blockchainStateService = blockchainStateService;
  }

  public Balance save(Balance balance) {
    return balanceRepository.save(balance);
  }

  public Balance findByAddress(String address, String networkName) {
    try {
      Optional<Balance> optionalBalance = balanceRepository.findByWalletAddressAndNetworkName(address, networkName);
      if (!optionalBalance.isPresent()) {

        // get balanceFrom network
        Pair<BigInteger, BigInteger> balanceFromNetwork = getBalanceFromNetwork(address, networkName);
        Balance balance = new Balance(networkName, balanceFromNetwork.getFirst(), balanceFromNetwork.getSecond());

        return balance;
      }
      Balance balance = optionalBalance.get();
      BigInteger lastBlockNumber = balance.getBlockNumber();

      // calculateBalance from events
      List<Event> events = this.eventService.findByAddress(address, networkName, lastBlockNumber);

      // update Balance
      BigInteger amount = balance.getAmount();
      for (Event event : events) {
        if (!event.getTxStatus().equals(Event.TransactionStatus.CONFIRMED)) {
          balance.setBlockNumber(event.getBlockNumber().subtract(BigInteger.ONE));
          continue;
        }

        if (event.getFromAddress().equals(address)) {
          amount = amount.subtract(event.getAmount());
        } else {
          amount = amount.add(event.getAmount());
        }
        balance.setBlockNumber(event.getBlockNumber());
      }
      balance.setAmount(amount);

      return balanceRepository.save(balance);

    } catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }

  public Pair<BigInteger, BigInteger> getBalanceFromNetwork(String address, String networkName) {
    try {
      Web3j web3j = web3jConfig.getWeb3jInstanceMap().get(networkName);
      BigInteger blockNumber = blockchainStateService.getLatestBlockNumber(networkName);

      // Create a DefaultBlockParameter for the block number
      DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(blockNumber.subtract(BigInteger.valueOf(12)));

      BigInteger balanceWei = web3j.ethGetBalance(address, blockParameter).send().getBalance();
      return Pair.of(balanceWei, blockNumber);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
