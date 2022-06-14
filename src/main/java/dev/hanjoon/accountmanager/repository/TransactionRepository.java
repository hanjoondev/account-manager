package dev.hanjoon.accountmanager.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.hanjoon.accountmanager.domain.Transaction;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

}