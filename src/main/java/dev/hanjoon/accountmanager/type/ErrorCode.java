package dev.hanjoon.accountmanager.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    CLIENT_NOT_FOUND("There is no client with requested username."),
    ACCOUNT_NOT_FOUND("There is no account with requested account number"),
    ACCOUNT_LIMIT_REACHED("Client already has the maximum number of accounts (10 accounts)"),
    ACCOUNT_CLOSED("Account is already closed"),
    ACCOUNT_WITH_BALANCE("Account has non-zero balance"),
    ACCOUNT_OWNED_BY_OTHER_CLIENT("Account is owned by someone else"),
    TRANSACTION_INSUFFICIENT_FUNDS("Account has insufficient balance to make the transaction"),
    TRANSACTION_BELOW_MIN_THRESHOLD("Transaction amount is below minimum threshold"),
    TRANSACTION_EXCEEDS_MAX_THRESHOLD("Transaction amount is above maximum threshold"),
    TRANSACTION_NOT_FOUND("There is no transaction with requested transaction id"),
    TRANSACTION_ACCOUNT_NOT_MATCHED("Transaction is made on another account"),
    TRANSACTION_INVALID_AMOUNT("Requested amount does not match the transaction record"),
    TRANSACTION_NOT_COMMITTED("Transaction is not committed yet or aborted or rolled back");
    private final String description;
}
