package com.example.wallet.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.*;

@ConfigurationProperties(prefix = "web3")
@Getter @Setter @NoArgsConstructor
public class Web3jProperties {
  private List<BlockchainInfo> blockchains = new ArrayList<>();

  @Getter @Setter
  public static class BlockchainInfo {
    private String networkName;
    private String providerUrl;
    private int chainId;
    private String symbol;
    private String explorerUrl;
  }
}

