package dev.hanjoon.accountmanager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.hanjoon.accountmanager.domain.Account;
import dev.hanjoon.accountmanager.domain.Client;
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

@SpringBootTest
public class AccountServiceTest {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    @Test
    @DisplayName("Create an account for a client without any account")
    public void createOneAccountTest() {
        Client testClient = Client.builder().username("testClientZero").build();
        clientRepository.save(testClient);

        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest("testClientZero", 1L));

        List<Account> accounts = accountRepository.findByClient(testClient);
        Account account = accounts.get(0);
        Assertions.assertEquals(1, accounts.size());
        Assertions.assertEquals(created.getAccountNumber(), account.getAccountNumber());
        Assertions.assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
        Assertions.assertEquals(1L, account.getBalance());
        Assertions.assertTrue(account.getClosedAt() == null);
        Assertions.assertTrue(account.getCreatedAt() != null && account.getCreatedAt().getClass() == LocalDateTime.class);
        Assertions.assertTrue(account.getUpdatedAt() != null && account.getUpdatedAt().getClass() == LocalDateTime.class);
        Assertions.assertEquals("testClientZero", account.getClient().getUsername());
    }

    @Test
    @DisplayName("Create 10 accounts for a client without any account")
    public void createTenAccountTest() {
        Client testClient = Client.builder().username("testClientTen").build();
        clientRepository.save(testClient);

        List<CreateAccountResponse> created = new ArrayList<>();
        for (long i = 1; i <= 10; i++)
            created.add(accountService.createAccount(
                new CreateAccountRequest("testClientTen", i)));

        List<Account> accounts = accountRepository.findByClient(testClient);
        for (int i = 1; i <= 10; i++) {
            Account account = accounts.get(i - 1);
            Assertions.assertEquals(created.get(i - 1).getAccountNumber(), account.getAccountNumber());
            Assertions.assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
            Assertions.assertEquals(i, account.getBalance());
            Assertions.assertTrue(account.getClosedAt() == null);
            Assertions.assertTrue(account.getCreatedAt() != null && account.getCreatedAt().getClass() == LocalDateTime.class);
            Assertions.assertTrue(account.getUpdatedAt() != null && account.getUpdatedAt().getClass() == LocalDateTime.class);
            Assertions.assertEquals("testClientTen", account.getClient().getUsername());
        }
    }

    @Test
    @DisplayName("Try to create 11th account for a client which already has 10 accoutns")
    public void createEleventhAccountTest() {
        Client testClient = Client.builder().username("testClientEleven").build();
        clientRepository.save(testClient);

        for (long i = 1; i <= 10; i++)
            accountService.createAccount(
                new CreateAccountRequest("testClientEleven", i));
        
        try {
            accountService.createAccount(
                new CreateAccountRequest("testClientEleven", 11L));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_LIMIT_REACHED, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to create an account for non-existing client")
    public void createAccountForNonExistingClientTest() {
        try {
            accountService.createAccount(
                new CreateAccountRequest("nonExistingClient", 0L));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.CLIENT_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Close an account for a client")
    public void closeAccountTest() {
        String clientUsername = "testCloseAccount";
        Long balance = 0L;
        Client testClient = Client.builder().username(clientUsername).build();
        clientRepository.save(testClient);
        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest(clientUsername, balance));
        
        List<Account> before = accountRepository.findByClient(testClient);
        Assertions.assertEquals(created.getAccountNumber(), before.get(0).getAccountNumber());
        Assertions.assertEquals(AccountStatus.ACTIVE, before.get(0).getAccountStatus());
        Assertions.assertEquals(balance, before.get(0).getBalance());

        CloseAccountResponse closed = accountService.closeAccount(
            new CloseAccountRequest(clientUsername, created.getAccountNumber()));

        List<Account> after = accountRepository.findByClient(testClient);
        Assertions.assertEquals(1, after.size());
        Assertions.assertEquals(closed.getAccountNumber(), after.get(0).getAccountNumber());
        Assertions.assertEquals(AccountStatus.CLOSED, after.get(0).getAccountStatus());
        Assertions.assertEquals(balance, after.get(0).getBalance());
        Assertions.assertTrue(after.get(0).getClosedAt() != null
                              && after.get(0).getClosedAt().getClass() == LocalDateTime.class);
    }

    @Test
    @DisplayName("Try to close an account for non-existing client")
    public void closeAccountForNonExistingClientTest() {
        clientRepository.save(Client.builder().username("realClient").build());
        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest("realClient", 0L));
        try {
            accountService.closeAccount(
                new CloseAccountRequest("nonExistingClient", created.getAccountNumber()));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.CLIENT_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to close non-existing account")
    public void closeNonExistingAccountTest() {
        String bogusAccountNumber = "ABCDEFGHIJ";
        clientRepository.save(Client.builder().username("existingClientWithAccount").build());
        accountService.createAccount(
            new CreateAccountRequest("existingClientWithAccount", 0L));
        try {
            accountService.closeAccount(
                new CloseAccountRequest("existingClientWithAccount", bogusAccountNumber));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to close an account belongs to someone else")
    public void closeAccountOwnedByOtherClient() {
        clientRepository.save(Client.builder().username("notAnOwner").build());
        clientRepository.save(Client.builder().username("accountOwner").build());
        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest("accountOwner", 1L));

        try {
            accountService.closeAccount(
                new CloseAccountRequest("notAnOwner", created.getAccountNumber()));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_OWNED_BY_OTHER_CLIENT, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to close an account which has been closed already")
    public void closeAlreadyClosedAccountTest() {
        String clientUsername = "testAlreadyClosed";
        Long balance = 0L;
        Client testClient = Client.builder().username(clientUsername).build();
        clientRepository.save(testClient);
        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest(clientUsername, balance));

        Optional<Account> account = accountRepository.findByAccountNumber(created.getAccountNumber());
        Assertions.assertTrue(account.isPresent());
        account.get().setAccountStatus(AccountStatus.CLOSED);
        account.get().setClosedAt(LocalDateTime.now());
        accountRepository.save(account.get());
        List<Account> before = accountRepository.findByClient(testClient);
        Assertions.assertEquals(created.getAccountNumber(), before.get(0).getAccountNumber());
        Assertions.assertEquals(AccountStatus.CLOSED, before.get(0).getAccountStatus());
        Assertions.assertEquals(balance, before.get(0).getBalance());
        Assertions.assertTrue(before.get(0).getClosedAt() != null
                              && before.get(0).getClosedAt().getClass() == LocalDateTime.class);

        try {
            accountService.closeAccount(
                new CloseAccountRequest(clientUsername, created.getAccountNumber()));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_CLOSED, e.getErrorCode());
        }
    }

    @Test
    @DisplayName("Try to close an account with non zero balance")
    public void closeNonZeroBalanceAccountTest() {
        String clientUsername = "testNonZeroBalance";
        Long balance = 1L;
        Client testClient = Client.builder().username(clientUsername).build();
        clientRepository.save(testClient);
        CreateAccountResponse created = accountService.createAccount(
            new CreateAccountRequest(clientUsername, balance));
        
        List<Account> before = accountRepository.findByClient(testClient);
        Assertions.assertEquals(created.getAccountNumber(), before.get(0).getAccountNumber());
        Assertions.assertEquals(AccountStatus.ACTIVE, before.get(0).getAccountStatus());
        Assertions.assertEquals(balance, before.get(0).getBalance());

        try {
            accountService.closeAccount(
                new CloseAccountRequest(clientUsername, created.getAccountNumber()));
            Assertions.fail("Should throw exception");
        } catch (AccountException e) {
            Assertions.assertEquals(ErrorCode.ACCOUNT_WITH_BALANCE, e.getErrorCode());
        }

        List<Account> after = accountRepository.findByClient(testClient);
        Assertions.assertEquals(1, after.size());
        Assertions.assertEquals(created.getAccountNumber(), after.get(0).getAccountNumber());
        Assertions.assertEquals(AccountStatus.ACTIVE, after.get(0).getAccountStatus());
        Assertions.assertEquals(balance, after.get(0).getBalance());
        Assertions.assertTrue(after.get(0).getClosedAt() == null);
    }

    @Test
    @DisplayName("Retrieve a list of accounts for three different clients")
    public void listAccountTest() {
        Client zeroAccountClient = Client.builder().username("testZeroAccount").build();
        Client oneAccountClient = Client.builder().username("testOneAccount").build();
        Client tenAccountClient = Client.builder().username("testTenAccount").build();
        clientRepository.save(zeroAccountClient);
        clientRepository.save(oneAccountClient);
        clientRepository.save(tenAccountClient);
        accountService.createAccount(
            new CreateAccountRequest("testOneAccount", 100L));
        for (long i = 101; i <= 110; i++)
            accountService.createAccount(
                new CreateAccountRequest("testTenAccount", i));

        ListAccountResponse zero = accountService.listAccount("testZeroAccount");
        ListAccountResponse one = accountService.listAccount("testOneAccount");
        ListAccountResponse ten = accountService.listAccount("testTenAccount");

        Assertions.assertEquals(0, zero.getAccounts().size());
        Assertions.assertEquals(1, one.getAccounts().size());
        Assertions.assertEquals(10, ten.getAccounts().size());
        Assertions.assertEquals(100L, one.getAccounts().get(0).getBalance());
        for (int i = 0; i < 10; i++)
            Assertions.assertEquals(i + 101L, ten.getAccounts().get(i).getBalance());
    }

    @Test
    @DisplayName("Retrieve a list of accounts for after adding a new account")
    public void listAccountAfterAddingNewAccountTest() {
        String clientUsername = "testListAccountAfterAddingNewAccount";
        clientRepository.save(Client.builder().username(clientUsername).build());
        accountService.createAccount(
            new CreateAccountRequest(clientUsername, 100L));
        ListAccountResponse one = accountService.listAccount(clientUsername);
        Assertions.assertEquals(1, one.getAccounts().size());
        Assertions.assertEquals(100L, one.getAccounts().get(0).getBalance());

        accountService.createAccount(
            new CreateAccountRequest(clientUsername, 101L));
        ListAccountResponse two = accountService.listAccount(clientUsername);
        Assertions.assertEquals(2, two.getAccounts().size());
        Assertions.assertEquals(100L, two.getAccounts().get(0).getBalance());
        Assertions.assertEquals(101L, two.getAccounts().get(1).getBalance());
    }

    
    @Test
    @DisplayName("Retrieve a list of accounts for after closing an account")
    public void listAccountAfterClosingAnAccountTest() {
        String clientUsername = "testListAccountAfterClosingAnAccount";
        clientRepository.save(Client.builder().username(clientUsername).build());
        for (long i = 101; i <= 110; i++)
            accountService.createAccount(
                new CreateAccountRequest(clientUsername, i != 106L ? i : 0));

        ListAccountResponse ten = accountService.listAccount(clientUsername);
        String accountNumberToBeDeleted = ten.getAccounts().get(5).getAccountNumber();
        Assertions.assertEquals(10, ten.getAccounts().size());
        Assertions.assertEquals(101L, ten.getAccounts().get(0).getBalance());
        Assertions.assertEquals(105L, ten.getAccounts().get(4).getBalance());
        Assertions.assertEquals(0L, ten.getAccounts().get(5).getBalance());
        Assertions.assertEquals(107L, ten.getAccounts().get(6).getBalance());
        Assertions.assertEquals(110L, ten.getAccounts().get(9).getBalance());
        accountService.closeAccount(
            new CloseAccountRequest(clientUsername, accountNumberToBeDeleted));

        ListAccountResponse nine = accountService.listAccount(clientUsername);
        Assertions.assertEquals(9, nine.getAccounts().size());
        Assertions.assertEquals(101L, nine.getAccounts().get(0).getBalance());
        Assertions.assertEquals(105L, nine.getAccounts().get(4).getBalance());
        Assertions.assertEquals(107L, nine.getAccounts().get(5).getBalance());
        Assertions.assertEquals(110L, nine.getAccounts().get(8).getBalance());
        Optional<Account> deleted = accountRepository.findByAccountNumber(accountNumberToBeDeleted);
        Assertions.assertTrue(deleted.isPresent());
        Assertions.assertEquals(AccountStatus.CLOSED, deleted.get().getAccountStatus());
    }
}