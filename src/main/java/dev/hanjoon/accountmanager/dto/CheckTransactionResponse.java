package dev.hanjoon.accountmanager.dto;

import java.time.LocalDateTime;

import dev.hanjoon.accountmanager.type.TransactionStatus;
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
public class CheckTransactionResponse {
    private Long transactionId;
    private String accountNumber;
    private TransactionStatus transactionStatus;
    private Long amount;
    private LocalDateTime createdAt;
}
