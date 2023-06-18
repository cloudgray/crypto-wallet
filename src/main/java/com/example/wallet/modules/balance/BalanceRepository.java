package com.example.wallet.modules.balance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
  @Query("SELECT b FROM Balance b JOIN b.wallet w WHERE w.address = :address AND b.networkName = :networkName")
  Optional<Balance> findByWalletAddressAndNetworkName(String address, String networkName);
}