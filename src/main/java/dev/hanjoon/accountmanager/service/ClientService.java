package dev.hanjoon.accountmanager.service;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import dev.hanjoon.accountmanager.domain.Account;
import dev.hanjoon.accountmanager.domain.Client;
import dev.hanjoon.accountmanager.repository.AccountRepository;
import dev.hanjoon.accountmanager.repository.ClientRepository;
import dev.hanjoon.accountmanager.type.AccountStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        Client clientWithoutAccount = Client.builder().username("clientWithoutAccount").build();
        Client clientWithOneAccount = Client.builder().username("clientWithOneAccount").build();
        Client clientWithTenAccount = Client.builder().username("clientWithTenAccount").build();
        clientRepository.save(clientWithoutAccount);
        clientRepository.save(clientWithOneAccount);
        clientRepository.save(clientWithTenAccount);
        accountRepository.save(Account.builder()
                                      .accountNumber("1000000000")
                                      .accountStatus(AccountStatus.ACTIVE)
                                      .balance(10_000L)
                                      .client(clientWithOneAccount)
                                      .build());
        for (int i = 1; i <= 10; i++) {
            accountRepository.save(Account.builder()
                                          .accountNumber(String.valueOf(1_000_000_000 + i))
                                          .accountStatus(AccountStatus.ACTIVE)
                                          .balance(i < 5 ? 10_000L : 0L)
                                          .client(clientWithTenAccount)
                                          .build());
        }
    }
}
