package dev.hanjoon.accountmanager.dto;

import java.time.LocalDateTime;

import dev.hanjoon.accountmanager.type.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendBalanceResponse {
    @Schema(example = "1000000000")
    private String accountNumber;
    @Schema(example = "COMMITTED")
    private TransactionStatus transactionStatus;
    @Schema(example = "1")
    private Long transactionId;
    @Schema(example = "5001")
    private Long amount;
    private LocalDateTime createdAt;
}
