package dev.hanjoon.accountmanager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.hanjoon.accountmanager.domain.Account;
import dev.hanjoon.accountmanager.domain.Client;
import dev.hanjoon.accountmanager.dto.AccountNumberAndBalance;
import dev.hanjoon.accountmanager.dto.CloseAccountRequest;
import dev.hanjoon.accountmanager.dto.CloseAccountResponse;
import dev.hanjoon.accountmanager.dto.CreateAccountRequest;
import dev.hanjoon.accountmanager.dto.CreateAccountResponse;
import dev.hanjoon.accountmanager.dto.ListAccountResponse;
import dev.hanjoon.accountmanager.exception.AccountException;
import dev.hanjoon.accountmanager.repository.AccountRepository;
import dev.hanjoon.accountmanager.repository.ClientRepository;
import dev.hanjoon.accountmanager.type.AccountStatus;
import dev.hanjoon.accountmanager.type.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        Client client = getClientByUsername(request.getClientUsername());
        List<Account> accounts = accountRepository.findByClient(client);
        int numAccounts = 0;
        for (Account account : accounts)
            if (account.getAccountStatus() != AccountStatus.CLOSED) numAccounts++;
        if (numAccounts >= 10) throw new AccountException(ErrorCode.ACCOUNT_LIMIT_REACHED);
        String newAccountNumber = generateRandomAccountNumber();
        Account saved = accountRepository.save(
                Account.builder()
                    .client(client)
                    .accountNumber(newAccountNumber)
                    .accountStatus(AccountStatus.ACTIVE)
                    .balance(request.getInitialBalance())
                    .build());
        return new CreateAccountResponse(client.getUsername(), saved.getAccountNumber(), saved.getCreatedAt());
    }

    @Transactional
    public CloseAccountResponse closeAccount(CloseAccountRequest request) {
        Client client = getClientByUsername(request.getClientUsername());
        Account account = getAccountByAccountNumber(request.getAccountNumber());
        if (!account.getClient().equals(client))
            throw new AccountException(ErrorCode.ACCOUNT_OWNED_BY_OTHER_CLIENT);
        if (account.getAccountStatus() == AccountStatus.CLOSED)
            throw new AccountException(ErrorCode.ACCOUNT_CLOSED);
        if (account.getBalance() != 0)
            throw new AccountException(ErrorCode.ACCOUNT_WITH_BALANCE);
        account.setAccountStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        accountRepository.save(account);
        return new CloseAccountResponse(client.getUsername(), account.getAccountNumber(), account.getClosedAt());
    }

    public ListAccountResponse listAccount(String clientUsername) {
        Client client = getClientByUsername(clientUsername);
        List<AccountNumberAndBalance> accounts = accountRepository.findByClient(client).stream()
                .filter(a -> a.getAccountStatus() != AccountStatus.CLOSED)
                .map(a -> new AccountNumberAndBalance(a.getAccountNumber(), a.getBalance()))
                .collect(Collectors.toList());
        return ListAccountResponse.builder()
                          .clientUsername(client.getUsername())
                          .accounts(accounts)
                          .build();
    }

    protected Client getClientByUsername(String username) {
        return clientRepository.findByUsername(username)
                .orElseThrow(() -> new AccountException(ErrorCode.CLIENT_NOT_FOUND));
    }

    protected Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private String generateRandomAccountNumber() {
        while (true) {
            String cand = String.valueOf(new Random().nextLong(9_000_000_000L) + 1_000_000_000L);
            if (accountRepository.findByAccountNumber(cand).isEmpty())
                return cand;
        }
    }
}
