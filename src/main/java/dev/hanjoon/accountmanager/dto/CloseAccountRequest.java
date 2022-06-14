package dev.hanjoon.accountmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CloseAccountRequest {
    @Schema(example = "clientWithTenAccount")
    private String clientUsername;
    @Schema(example = "1000000009")
    private String accountNumber;
}
