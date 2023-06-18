package com.example.wallet.modules.blockchain_state;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "blockchains")
public class BlockchainState {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "blockchain_id")
  private Long id;

  @Column(name = "network_name")
  private String networkName;
  
  @Column(name = "next_block_number")
  private BigInteger nextBlockNumber;

  public BlockchainState(String networkName, BigInteger nextBlockNumber) {
    this.networkName = networkName;
    this.nextBlockNumber = nextBlockNumber;
  }
}
