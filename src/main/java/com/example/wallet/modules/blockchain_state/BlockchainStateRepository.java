package com.example.wallet.modules.blockchain_state;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockchainStateRepository extends JpaRepository<BlockchainState, Long>  {
  BlockchainState findByNetworkName(String networkName);
}
