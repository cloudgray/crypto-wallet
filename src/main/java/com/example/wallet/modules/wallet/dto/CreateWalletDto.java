package com.example.wallet.modules.wallet.dto;

import lombok.Getter;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Component
@Getter @Setter @NoArgsConstructor @AllArgsConstructor 
public class CreateWalletDto {
    @NotBlank
    private String password;
}

