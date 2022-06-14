package dev.hanjoon.accountmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.hanjoon.accountmanager.domain.Account;
import dev.hanjoon.accountmanager.domain.Client;


@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findByClient(Client client);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findFirstByOrderByIdDesc();
}
