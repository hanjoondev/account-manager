package dev.hanjoon.accountmanager.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.hanjoon.accountmanager.domain.Account;
import dev.hanjoon.accountmanager.domain.Client;
import dev.hanjoon.accountmanager.domain.Transaction;
import dev.hanjoon.accountmanager.dto.AbortTransactionRequest;
import dev.hanjoon.accountmanager.dto.AbortTransactionResponse;
import dev.hanjoon.accountmanager.dto.CheckTransactionResponse;
import dev.hanjoon.accountmanager.dto.SpendBalanceRequest;
import dev.hanjoon.accountmanager.dto.SpendBalanceResponse;
import dev.hanjoon.accountmanager.exception.AccountException;
import dev.hanjoon.accountmanager.exception.TransactionException;
import dev.hanjoon.accountmanager.repository.AccountRepository;
import dev.hanjoon.accountmanager.repository.TransactionRepository;
import dev.hanjoon.accountmanager.type.AccountStatus;
import dev.hanjoon.accountmanager.type.ErrorCode;
import dev.hanjoon.accountmanager.type.TransactionStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public SpendBalanceResponse spendBalance(SpendBalanceRequest request) {
        Client client = accountService.getClientByUsername(
            request.getClientUsername());
        Account account = accountService.getAccountByAccountNumber(
            request.getAccountNumber());
        if (!account.getClient().getUsername().equals(client.getUsername()))
            throw new AccountException(
                ErrorCode.ACCOUNT_OWNED_BY_OTHER_CLIENT);
        if (account.getAccountStatus() == AccountStatus.CLOSED)
            throw new AccountException(
                ErrorCode.ACCOUNT_CLOSED);
        if (account.getBalance() < request.getAmount())
            throw new TransactionException(
                ErrorCode.TRANSACTION_INSUFFICIENT_FUNDS);
        if (request.getAmount() < 100L)
            throw new TransactionException(
                ErrorCode.TRANSACTION_BELOW_MIN_THRESHOLD);
        else if (request.getAmount() > 1_000_000_000L)
            throw new TransactionException(
                ErrorCode.TRANSACTION_EXCEEDS_MAX_THRESHOLD);
        Transaction transaction = Transaction.builder()
                                             .account(account)
                                             .transactionStatus(TransactionStatus.ACTIVE)
                                             .amount(request.getAmount())
                                             .build();
        account.setBalance(account.getBalance() - request.getAmount());
        transactionRepository.save(transaction);
        accountRepository.save(account);
        transaction.setTransactionStatus(TransactionStatus.COMMITTED);
        return new SpendBalanceResponse(account.getAccountNumber(),
                                      transaction.getTransactionStatus(),
                                      transaction.getId(),
                                      transaction.getAmount(),
                                      transaction.getCreatedAt());
    }

    @Transactional
    public AbortTransactionResponse abortTransaction(AbortTransactionRequest request) {
        Account account = accountService.getAccountByAccountNumber(
            request.getAccountNumber());
        Transaction transaction = transactionRepository.findById(
            request.getTransactionId())
                   .orElseThrow(() -> new TransactionException(
                                ErrorCode.TRANSACTION_NOT_FOUND));
        if (!transaction.getAccount().equals(account))
            throw new TransactionException(
                ErrorCode.TRANSACTION_ACCOUNT_NOT_MATCHED);
        if (!transaction.getAmount().equals(request.getAmount()))
            throw new TransactionException(
                ErrorCode.TRANSACTION_INVALID_AMOUNT);
        if (transaction.getTransactionStatus() != TransactionStatus.COMMITTED)
            throw new TransactionException(
                ErrorCode.TRANSACTION_NOT_COMMITTED);
        transaction.setTransactionStatus(TransactionStatus.ABORTED);
        transaction.setAbortedAt(LocalDateTime.now());
        account.setBalance(account.getBalance() + request.getAmount());
        accountRepository.save(account);
        transactionRepository.save(transaction);
        return new AbortTransactionResponse(account.getAccountNumber(),
                                        transaction.getTransactionStatus(),
                                        transaction.getId(),
                                        transaction.getAmount(),
                                        transaction.getAbortedAt());
    }

    public CheckTransactionResponse checkTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                   .orElseThrow(() -> new TransactionException(
                                ErrorCode.TRANSACTION_NOT_FOUND));
        return new CheckTransactionResponse(transaction.getId(),
                                            transaction.getAccount().getAccountNumber(),
                                            transaction.getTransactionStatus(),
                                            transaction.getAmount(),
                                            transaction.getCreatedAt());
    }
}
