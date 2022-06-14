package dev.hanjoon.accountmanager.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class CreateAccountResponse {
    @Schema(example = "clientWithOneAccount")
    private String clientUsername;
    @Schema(example = "1000000011")
    private String accountNumber;
    private LocalDateTime createdAt;
}
