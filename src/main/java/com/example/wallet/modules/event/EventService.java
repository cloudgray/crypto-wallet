package com.example.wallet.modules.event;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.web3j.protocol.core.methods.response.Transaction;

import com.example.wallet.modules.event.Event.TransactionStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class EventService {
  private static final Logger logger = LogManager.getLogger(EventService.class);
  private final EventRepository eventRepository;

  @Autowired
  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  public Event createPendingEvent(String networkName, String txHash, String fromAddress, String toAddress, BigInteger amount) {
    try {
      Event event = new Event(networkName, txHash, TransactionStatus.PENDING, BigInteger.ZERO);
            event.setFromAddress(fromAddress);
            event.setToAddress(toAddress);
            event.setAmount(amount);
      return eventRepository.save(event);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  public void createMinedEvent(Transaction tx, BigInteger blockNumber) {
    try {
      Event event = new Event();
            event.setNetworkName("ethereum_goerli");
            event.setFromAddress(tx.getFrom());
            event.setToAddress(tx.getTo());
            event.setAmount(tx.getValue());  
            event.setTxHash(tx.getHash());
            event.setTxStatus(TransactionStatus.MINED);  
            event.setBlockNumber(blockNumber);
            event.setBlockConfirmations(BigInteger.ZERO);
      eventRepository.save(event);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
    
  }

  public void save(Event event) {
    try {
      eventRepository.save(event);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  public void createConfirmedEvent(Event minedEvent) {
    try {
      Event event = new Event();
            event.setNetworkName(minedEvent.getNetworkName());
            event.setFromAddress(minedEvent.getFromAddress());
            event.setToAddress(minedEvent.getToAddress());
            event.setAmount(minedEvent.getAmount());
            event.setBlockNumber(minedEvent.getBlockNumber());
            event.setTxHash(minedEvent.getTxHash());
            event.setTxStatus(TransactionStatus.CONFIRMED);
            event.setBlockConfirmations(BigInteger.valueOf(12));
      eventRepository.save(event);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  public Page<Event> findByAddressWithPagination(String address, Optional<Long> startingAfter, Optional<Long> endingBefore, Optional<Integer> size) {
    try {
      int pageSize = size.orElse(10);
      if (pageSize > 100) pageSize = 100;
      if (pageSize < 1) pageSize = 10;

      Sort sort = Sort.by(Sort.Direction.DESC, "id");
      Pageable pageable = PageRequest.of(0, pageSize, sort);
      if (startingAfter.isPresent() && startingAfter.get() != 0L) {
        return eventRepository.findByAddressAndStartingAfter(address, startingAfter.get(), pageable);
      } else if (endingBefore.isPresent() && endingBefore.get() != 0L) {
        return eventRepository.findByAddressAndEndingBefore(address, endingBefore.get(), pageable);
      } else {
        return eventRepository.findByAddress(address, pageable);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
    
  }

  public List<Event> findByAddress(String address, String networkName, BigInteger blockNumber) {
    try {
      return eventRepository.findByAddressAndBlockNumber(address, networkName, blockNumber);
    } catch (Exception e) {
      logger.error(e.getMessage());
      logger.error(e.getMessage());
      throw e;
    }
  }

  public List<Event> findLessThan12Confirmations(String networkName, Event.TransactionStatus txStatus) {
    try {
      return eventRepository.findLessThan12Confirmations(networkName, txStatus);
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw e;
    }
  }

  public List<Event> findOwnLessThan12Confirmations(String networkName, String fromAddress, Event.TransactionStatus txStatus) {
    try {
      return eventRepository.findOwnLessThan12Confirmations(networkName, fromAddress, txStatus);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
      throw e;
    }
  }
}
