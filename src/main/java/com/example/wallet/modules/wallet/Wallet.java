package com.example.wallet.modules.wallet;

import lombok.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.example.wallet.modules.balance.Balance;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "wallets", uniqueConstraints = {@UniqueConstraint(columnNames = "address")})
@Getter @Setter @NoArgsConstructor
@ToString
public class Wallet {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_id")
  private Long id;

  @Column(name = "address", unique = true)
  private String address;

  @Column(name = "encrypted_private_key")
  private String encryptedPrivateKey;

  @Column(name = "hashed_password")
  private String hashedPassword;

  @Column(name = "nonce")
  private BigInteger nonce = BigInteger.ZERO;

  @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference
  private List<Balance> balances = new ArrayList<>();

  public Wallet(String address, String encryptedPrivateKey, String hashedPassword) {
    this.address = address;
    this.encryptedPrivateKey = encryptedPrivateKey;
    this.hashedPassword = hashedPassword;
  }
}
