package dev.hanjoon.accountmanager.dto;

import java.util.List;

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
public class ListAccountResponse {
    @Schema(example = "clientWithTenAccount")
    private String clientUsername;
    private List<AccountNumberAndBalance> accounts;
}
