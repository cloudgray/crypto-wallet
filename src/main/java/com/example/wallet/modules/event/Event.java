package com.example.wallet.modules.event;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "events", schema = "public", indexes = {
  @Index(name = "idx_from_address", columnList = "from_address"),
  @Index(name = "idx_to_address", columnList = "to_address")
})
@Getter @Setter @NoArgsConstructor
@ToString
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_id")
  private Long id;

  @Column(name = "network_name")
  private String networkName;

  @Column(name = "from_address")
  private String fromAddress;

  @Column(name = "to_address")
  private String toAddress;

  @Column(name = "amount")
  private BigInteger amount;

  @Column(name = "block_number")
  private BigInteger blockNumber;

  @Column(name = "tx_hash")
  private String txHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "tx_status")
  private TransactionStatus txStatus;

  @Column(name = "block_confirmations")
  private BigInteger blockConfirmations;

  public Event(String networkName, String txHash, TransactionStatus txStatus, BigInteger blockConfirmations) {
    this.networkName = networkName;
    this.txHash = txHash;
    this.txStatus = txStatus;
    this.blockConfirmations = blockConfirmations;
  }

  public enum TransactionStatus {
    PENDING,
    MINED,
    CONFIRMED
  }
}
