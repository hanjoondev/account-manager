package dev.hanjoon.accountmanager.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.hanjoon.accountmanager.domain.Client;
import dev.hanjoon.accountmanager.dto.AbortTransactionRequest;
import dev.hanjoon.accountmanager.dto.CloseAccountRequest;
import dev.hanjoon.accountmanager.dto.CreateAccountRequest;
import dev.hanjoon.accountmanager.dto.CreateAccountResponse;
import dev.hanjoon.accountmanager.dto.SpendBalanceRequest;
import dev.hanjoon.accountmanager.dto.SpendBalanceResponse;
import dev.hanjoon.accountmanager.exception.AccountException;
import dev.hanjoon.accountmanager.exception.TransactionException;
import dev.hanjoon.accountmanager.repository.AccountRepository;
import dev.hanjoon.accountmanager.repository.ClientRepository;
import dev.hanjoon.accountmanager.repository.TransactionRepository;
import dev.hanjoon.accountmanager.type.AccountStatus;
import dev.hanjoon.accountmanager.type.ErrorCode;
import dev.hanjoon.accountmanager.type.TransactionStatus;

@SpringBootTest
public class TransactionServiceTest {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;

    @Test
    @DisplayName("Spend some balance from an account")
    public void spendBalanceTest() {
        String clientUsername = "spendTestClient";
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());

        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();

        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }

    @Test
    @DisplayName("Try to spend some balance from a someone else's account")
    public void spendFromAccountOwnedBySomeoneElseTest() {
        String clientUsername = "spendTestOwnerClient";
        String notAnOnwerClientUsername = "spendTestNotAnOwnerClient";
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        clientRepository.save(
            Client.builder().username(notAnOnwerClientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        
        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    notAnOnwerClientUsername,
                    accountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_OWNED_BY_OTHER_CLIENT, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
    }

    @Test
    @DisplayName("Try to spend some balance from an already closed account")
    public void spendBalanceFromAlreadyClosedAccount() {
        String clientUsername = "spendFromAlreadyClosedAccountTestClient";
        Long initialBalance = 0L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        accountService.closeAccount(
            new CloseAccountRequest(clientUsername, accountNumber));
        Assertions.assertEquals(AccountStatus.CLOSED,
            accountRepository.findByAccountNumber(accountNumber).get().getAccountStatus());
        
        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    clientUsername,
                    accountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_CLOSED, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to spend some balance from a non existing account")
    public void spendBalanceFromNonExistingAccount() {
        String clientUsername = "spendFromNonExistingAccountTestClient";
        String nonExistingAccountNumber = "0999999999";
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));

        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    clientUsername,
                    nonExistingAccountNumber,
                    amountToSpend));
                Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to spend too little (below minimum threshold) from an account")
    public void spendLessThanMinThresholdTest() {
        String clientUsername = "spendLessThanMinThresholdTestClient";
        Long initialBalance = 1_000_000L, belowMinThreshold = 99L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());

        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    clientUsername,
                    accountNumber,
                    belowMinThreshold));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_BELOW_MIN_THRESHOLD, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
    }

    @Test
    @DisplayName("Try to spend too much (exceeds maximum threshold) from an account")
    public void spendMoreThanMaxThresholdTest() {
        String clientUsername = "spendMoreThanMaxThresholdTestClient";
        Long initialBalance = 1_000_000_000_000L, exceedsMaxThreshold = 1_000_000_001L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());

        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    clientUsername,
                    accountNumber,
                    exceedsMaxThreshold));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_EXCEEDS_MAX_THRESHOLD, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
    }

    @Test
    @DisplayName("Try to spend too much from an account (more than its balance)")
    public void spendMoreThanBalanceTest() {
        String clientUsername = "spendMoreThanBalanceTestClient";
        Long initialBalance = 1_000_000L, amountExceedsMaxThreshold = 1_000_001L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());

        try {
            transactionService.spendBalance(
                new SpendBalanceRequest(
                    clientUsername,
                    accountNumber,
                    amountExceedsMaxThreshold));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_INSUFFICIENT_FUNDS, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
    }

    @Test
    @DisplayName("Abort the comitted transaction")
    public void abortTransactionTest() {
        String clientUsername = "abortTransactionTestClient";
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();
        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
        
        transactionService.abortTransaction(
            new AbortTransactionRequest(
                transactionId,
                accountNumber,
                amountToSpend));
        Assertions.assertEquals(initialBalance,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.ABORTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }

    @Test
    @DisplayName("Try to abort a non-existing transaction")
    public void abortNonExistingTransactionTest() {
        String clientUsername = "abortNonExistingTransactionTestClient";
        Long nonExistingTransactionId = -1L;
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();
        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
        
        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    nonExistingTransactionId,
                    accountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }

    @Test
    @DisplayName("Try to abort a transaction while providing wrong account numbers")
    public void abortTransactionWithWrongAccountNumber() {
        String clientUsername = "abortTransactionWithWrongAccountNumberTestClient";
        String nonExistingAccountNumber = "0999999998";
        Long initialBalance = 1_000_000L, amountToSpend = 100L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        CreateAccountResponse otherAccount = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        String otherAccountNumber = otherAccount.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();
        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(initialBalance,
            accountRepository.findByAccountNumber(otherAccountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());

        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    transactionId,
                    otherAccountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_ACCOUNT_NOT_MATCHED, e.getErrorCode());
        }
        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    transactionId,
                    nonExistingAccountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(initialBalance,
            accountRepository.findByAccountNumber(otherAccountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }

    @Test
    @DisplayName("Try to abort a transaction while providing incorrect amounts")
    public void abortTransactionInvalidAmounts() {
        String clientUsername = "abortTransactionInvalidAmountsTestClient";
        Long initialBalance = 1_000_000L, amountToSpend = 1_000L;
        Long invalidAmountLessThanActual = 500L, invalidAmountMoreThanActual = 1_500L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();
        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());

        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    transactionId,
                    accountNumber,
                    invalidAmountLessThanActual));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_INVALID_AMOUNT, e.getErrorCode());
        }

        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    transactionId,
                    accountNumber,
                    invalidAmountMoreThanActual));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_INVALID_AMOUNT, e.getErrorCode());
        }

        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }

    @Test
    @DisplayName("Try to abort a transaction which already has been aborted")
    public void abortTransactionAlreadyAbortedTest() {
        String clientUsername = "abortTransactionAlreadyAbortedTestClient";
        Long initialBalance = 1_000_000L, amountToSpend = 1_000L;
        clientRepository.save(
            Client.builder().username(clientUsername).build());
        CreateAccountResponse account = accountService.createAccount(
            new CreateAccountRequest(clientUsername, initialBalance));
        String accountNumber = account.getAccountNumber();
        Assertions.assertEquals(initialBalance, 
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        SpendBalanceResponse response = transactionService.spendBalance(
            new SpendBalanceRequest(
                clientUsername,
                accountNumber,
                amountToSpend));
        Long transactionId = response.getTransactionId();
        Assertions.assertEquals(initialBalance - amountToSpend,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.COMMITTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
        
        transactionService.abortTransaction(
            new AbortTransactionRequest(
                transactionId,
                accountNumber,
                amountToSpend));
        Assertions.assertEquals(initialBalance,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.ABORTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());

        try {
            transactionService.abortTransaction(
                new AbortTransactionRequest(
                    transactionId,
                    accountNumber,
                    amountToSpend));
            Assertions.fail("Should throw exception");
        } catch (TransactionException e) {
            Assertions.assertEquals(ErrorCode.TRANSACTION_NOT_COMMITTED, e.getErrorCode());
        }
        Assertions.assertEquals(initialBalance,
            accountRepository.findByAccountNumber(accountNumber).get().getBalance());
        Assertions.assertEquals(TransactionStatus.ABORTED,
            transactionRepository.findById(transactionId).get().getTransactionStatus());
    }
}
