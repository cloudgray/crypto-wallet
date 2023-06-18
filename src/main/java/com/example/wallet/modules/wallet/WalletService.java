package com.example.wallet.modules.wallet;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import com.example.wallet.modules.balance.Balance;
import com.example.wallet.modules.balance.BalanceService;
import com.example.wallet.modules.blockchain_state.BlockchainStateService;
import com.example.wallet.modules.event.Event;
import com.example.wallet.modules.event.EventService;
import com.example.wallet.modules.event.Event.TransactionStatus;
import com.example.wallet.modules.transaction.TransactionService;
import com.example.wallet.utils.CryptoUtils;

import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class WalletService {
  private static final Logger logger = LogManager.getLogger(WalletService.class);
  private final WalletRepository walletRepository;
  private final BlockchainStateService blockchainStateService;
  private final EventService eventService;
  private final BalanceService balanceService;
  private final TransactionService transactionService;

  final String defaultNetworkName = "ethereum_goerli";

  @Autowired
  public WalletService(
    WalletRepository walletRepository, 
    BlockchainStateService blockchainStateService, 
    EventService eventService, 
    BalanceService balanceService,
    TransactionService transactionService
  ) {
    this.walletRepository = walletRepository;
    this.blockchainStateService = blockchainStateService;
    this.eventService = eventService;
    this.balanceService = balanceService;
    this.transactionService = transactionService;
  }

  public ResponseEntity<String> create(String password) {
    try {
      ECKeyPair keyPair = Keys.createEcKeyPair();
      String privateKey = keyPair.getPrivateKey().toString(16);
      String encryptedPrivateKey = CryptoUtils.encrypt(privateKey, password);
    
      String address = "0x" + Keys.getAddress(keyPair);

      // Hash the provided password
      String hashedPassword = CryptoUtils.hash(password);

      // get latest blockNumber
      BigInteger latestBlockNumber = blockchainStateService.getLatestBlockNumber(defaultNetworkName);
      
      // DB에 지갑 주소를 저장합니다.
      Wallet wallet = new Wallet(address, encryptedPrivateKey, hashedPassword);
      Balance balance = new Balance(wallet, "ethereum_goerli", BigInteger.ZERO, latestBlockNumber);
      wallet.getBalances().add(balance);
      walletRepository.save(wallet);

      // Private Key는 저장하지 않고 직접 사용자에게 반환해야 합니다.
      // 지갑 주소와 Private Key를 반환합니다.
      logger.info("Wallet created : " + address);
      return ResponseEntity.status(HttpStatus.CREATED).body("Address: " + address + ", Private Key: " + privateKey);

    } catch (Exception e) {
      logger.error("Failed to create wallet : " + e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  public Wallet findOne(Long walletId) {
    return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
  }

  public Wallet findByAddress(String address) {
    try {
      Wallet wallet = walletRepository.findByAddress(address);
      if (wallet == null) {
        logger.debug("Failed to find wallet : wallet not found");
        return null;
      }
      return wallet;
    } catch (Exception e) {
      logger.error("Failed to find wallet : " + e.getMessage(), e);
      return null;
    }
    
  }

  public List<Wallet> findAll() {
    return walletRepository.findAll();
  }

  public ResponseEntity<Page<Event>> getEventsByAddress(String address, Optional<Long> startingAfter, Optional<Long> endingBefore, Optional<Integer> size) {
    try {
      // find wallet
      Wallet wallet = walletRepository.findByAddress(address);
      if (wallet == null) {
        logger.debug("Failed to get events : wallet not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }

      // find events 
      Page<Event> pagedEvents =  eventService.findByAddressWithPagination(wallet.getAddress(), startingAfter, endingBefore, size);
      return ResponseEntity.status(HttpStatus.OK).body(pagedEvents);
    } catch (Exception e) {
      logger.error("Failed to get events : " + e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    } 
  }

  public ResponseEntity<Balance> getBalance(String address, String networkName) {
    if (networkName == null) networkName = defaultNetworkName;

    try {
      Wallet wallet = walletRepository.findByAddress(address);
      if (wallet == null) {
        logger.debug("Failed to get balance : wallet not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
      
      Balance balance = balanceService.findByAddress(address, networkName);
      if (balance.getWallet() == null) {
        balance.setWallet(wallet);
        balanceService.save(balance);
      }
      return ResponseEntity.status(HttpStatus.OK).body(balance);
    } catch (Exception e) {
      logger.error("Failed to get balance : " + e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @Transactional
  public ResponseEntity<String> transfer(String password, String networkName, String fromAddress, String toAddress, BigInteger amount) {
    try { 
      Wallet wallet = findByAddress(fromAddress);
      if (wallet == null) {
        logger.debug("transfer failed : wallet [{}] not found", fromAddress);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("transfer failed : wallet [{}] not found", fromAddress));
      }

      // Check password
      String hashedInputPassword = CryptoUtils.hash(password);
      if (!wallet.getHashedPassword().equals(hashedInputPassword)) {
        logger.debug("transfer failed : password doesn't match");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("transfer failed : password doesn't match");
      }

      // Decrypt private key
      String decryptedPrivateKey = CryptoUtils.decrypt(wallet.getEncryptedPrivateKey(), password);
      Credentials credentials = Credentials.create(decryptedPrivateKey);

      // Get balance
      Balance balance = balanceService.findByAddress(fromAddress, networkName);

      // Get pending events whose block confirmations are less than 12
      List<Event> pendingEvents = eventService.findOwnLessThan12Confirmations(networkName, fromAddress, TransactionStatus.PENDING);
      BigInteger pendingAount = new BigInteger("0");
      pendingEvents.forEach(event -> {
        pendingAount.add(event.getAmount());
      });
      BigInteger availableAmount = balance.getAmount().subtract(pendingAount);

      // Check balance
      if (availableAmount.compareTo(amount) < 0) {
        logger.debug("transfer failed : insufficient balance");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("transfer failed : insufficient balance");
      }

      // Send Transaction
      String txHash = transactionService.sendTransaction(networkName, credentials, fromAddress, toAddress, amount);
      if (txHash == null) {
        logger.debug("transfer failed : transaction failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("transfer failed. insufficient amount + gas * gasPrice.");
      }

      // Create an event for this transaction
      eventService.createPendingEvent(networkName, txHash, fromAddress, toAddress, amount);

      return ResponseEntity.status(HttpStatus.CREATED).body(txHash);
    } catch (Exception e){
      logger.error("transfer failed : " + e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("transfer failed : transaction failed : " + e.getMessage());
    } 
  }
}
