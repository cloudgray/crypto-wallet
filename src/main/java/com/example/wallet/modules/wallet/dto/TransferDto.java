package com.example.wallet.modules.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor
public class TransferDto {
  @NotNull
  private String to;

  @NotNull
  private String amount;

  @NotNull
  private String password;

  @JsonProperty("network_name")
  private String networkName;
}
