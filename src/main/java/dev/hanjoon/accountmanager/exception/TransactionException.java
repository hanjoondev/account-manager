package dev.hanjoon.accountmanager.exception;

import dev.hanjoon.accountmanager.type.ErrorCode;
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
public class TransactionException extends RuntimeException {
    private ErrorCode errorCode;
    private String errorMsg;

    public TransactionException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMsg = errorCode.getDescription();
    }
}
