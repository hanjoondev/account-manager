package dev.hanjoon.accountmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpendBalanceRequest {
    @Schema(example = "clientWithOneAccount")
    private String clientUsername;
    @Schema(example = "1000000000")
    private String accountNumber;
    @Schema(example = "5001")
    private Long amount;
}
