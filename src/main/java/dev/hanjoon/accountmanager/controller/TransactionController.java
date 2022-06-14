package dev.hanjoon.accountmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.hanjoon.accountmanager.dto.AbortTransactionRequest;
import dev.hanjoon.accountmanager.dto.AbortTransactionResponse;
import dev.hanjoon.accountmanager.dto.CheckTransactionResponse;
import dev.hanjoon.accountmanager.dto.SpendBalanceRequest;
import dev.hanjoon.accountmanager.dto.SpendBalanceResponse;
import dev.hanjoon.accountmanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Spend balance",
               description = "")
    @PostMapping(value = "api/transaction/spend", produces = "application/json")
    public SpendBalanceResponse spendBalance(
        @RequestBody @Valid SpendBalanceRequest request) {
        return transactionService.spendBalance(request);
    }

    @Operation(summary = "Abort transaction",
               description = "")
    @PostMapping(value = "api/transaction/abort", produces = "application/json")
    public AbortTransactionResponse abortTransaction(
        @RequestBody @Valid AbortTransactionRequest request) {
        return transactionService.abortTransaction(request);
    }

    @Operation(summary = "Check transaction detail",
               description = "Retrieve a transaction detail if there is a transaction with the provided transactionId")
    @GetMapping(value = "api/transaction/{transactionId}", produces = "application/json")
    public CheckTransactionResponse listAccount(
        @Parameter(required=true, example = "1") 
        @PathVariable Long transactionId) {
        return transactionService.checkTransaction(transactionId);
    }
}
