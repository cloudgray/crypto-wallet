package com.example.wallet.modules.event;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
  @Query("SELECT e FROM Event e WHERE (e.fromAddress = :address OR e.toAddress = :address) " +
        "ORDER BY e.id DESC")
  Page<Event> findByAddress(String address, Pageable pageable);

  @Query("SELECT e FROM Event e WHERE (e.fromAddress = :address OR e.toAddress = :address) " +
        "AND e.id < :endingBefore " +
        "ORDER BY e.id DESC")
  Page<Event> findByAddressAndEndingBefore(@Param("address") String address, @Param("endingBefore") Long endingBefore, Pageable pageable);

  @Query("SELECT e FROM Event e WHERE (e.fromAddress = :address OR e.toAddress = :address) " +
          "AND e.id > :startingAfter " +
          "ORDER BY e.id DESC")
  Page<Event> findByAddressAndStartingAfter(@Param("address") String address, @Param("startingAfter") Long startingAfter, Pageable pageable);

  @Query(value = "SELECT e FROM Event e " + 
               "WHERE e.networkName = :networkName AND (e.fromAddress = :address OR e.toAddress = :address) AND e.blockNumber > :blockNumber " + 
               "ORDER BY e.blockNumber ASC")
  List<Event> findByAddressAndBlockNumber(@Param("address") String address, @Param("networkName") String networkName, @Param("blockNumber") BigInteger blockNumber);


  List<Event> findByNetworkNameAndTxStatus(String networkName, Event.TransactionStatus txStatus);

  @Query("SELECT e FROM Event e WHERE e.networkName = :networkName AND e.txStatus = :txStatus AND e.blockConfirmations < 12")
  List<Event> findLessThan12Confirmations(@Param("networkName") String networkName, @Param("txStatus") Event.TransactionStatus txStatus);

  @Query("SELECT e FROM Event e WHERE e.fromAddress = :fromAddress AND e.networkName = :networkName AND e.txStatus = :txStatus AND e.blockConfirmations < 12")
  List<Event> findOwnLessThan12Confirmations(@Param("networkName") String networkName, @Param("fromAddress") String fromAddress, @Param("txStatus") Event.TransactionStatus txStatus);
}
