package com.example.wallet.modules.transaction;

import java.math.BigInteger;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import com.example.wallet.config.Web3jConfig;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {
  Web3jConfig web3jConfig;
  Web3j web3j;

  public TransactionService(Web3jConfig web3jConfig) {
    this.web3jConfig = web3jConfig;
  }

  @Transactional
  public String sendTransaction(String networkName, Credentials credentials, String fromAddress, String toAddress, BigInteger amount) {
    try { 
      // Get the current nonce
      Web3j web3j = web3jConfig.getWeb3jInstanceMap().get(networkName);
      EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST).send();
      BigInteger nonce = ethGetTransactionCount.getTransactionCount();

      // Create a RawTransactionManager
      RawTransactionManager rawTransactionManager = new RawTransactionManager(
              web3j,
              credentials,
              nonce.intValue(),
              200  // sleepDuration
      );

      // Build and sign the raw transaction
      RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
              nonce,
              DefaultGasProvider.GAS_PRICE,
              DefaultGasProvider.GAS_LIMIT,
              toAddress,
              amount
      );
      
      String signedTxHex = rawTransactionManager.sign(rawTransaction);

      EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedTxHex).send();

      // Check for transaction error
      if (ethSendTransaction.hasError()) {
          throw new RuntimeException("Error processing transaction request: " + ethSendTransaction.getError().getMessage());
      }

      return ethSendTransaction.getTransactionHash();
    } catch (Exception e){
      e.printStackTrace();
      return null;
    } 
  }
}
