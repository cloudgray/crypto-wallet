package com.example.wallet.modules.balance;

import java.math.BigInteger;

import com.example.wallet.modules.wallet.Wallet;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "balances")
@Getter @Setter @NoArgsConstructor
@ToString
public class Balance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "balance_id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "wallet_address", referencedColumnName = "address")
  @JsonBackReference
  private Wallet wallet;

  @Column(name = "network_name")
  private String networkName;

  @Column(name = "amount")
  private BigInteger amount;

  @Column(name = "block_number")
  private BigInteger blockNumber;

  public Balance(Wallet wallet, String networkName, BigInteger amount, BigInteger blockNumber) {
    this.wallet = wallet;
    this.networkName = networkName;
    this.amount = amount;
    this.blockNumber = blockNumber;
  }

  public Balance(String networkName, BigInteger amount, BigInteger blockNumber) {
    this.networkName = networkName;
    this.amount = amount;
    this.blockNumber = blockNumber;
  }
}
