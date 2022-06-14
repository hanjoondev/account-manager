package dev.hanjoon.accountmanager.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.hanjoon.accountmanager.dto.CreateAccountRequest;
import dev.hanjoon.accountmanager.dto.CreateAccountResponse;
import dev.hanjoon.accountmanager.dto.CloseAccountRequest;
import dev.hanjoon.accountmanager.dto.CloseAccountResponse;
import dev.hanjoon.accountmanager.dto.ListAccountResponse;
import dev.hanjoon.accountmanager.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Create an account",
               description = "Create an account if;<br>"
               + "0. Request JSON is not malformed (contains both clientUsername and balance, no error on syntax/type, etc)<br>"
               + "1. There is a client with the provided clientUsername<br>"
               + "2. and the client has less than 10 active accounts<br>"
               + "There are some pre-made clients and accounts for demo purposes.<br />"
               + "clientUsername: \"clientWithoutAccount\", \"clientWithOneAccount\", \"clientWithTenAccount\"<br />")
    @PostMapping(value = "api/account", produces = "application/json")
    public CreateAccountResponse createAccount(
        @RequestBody @Valid CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @Operation(summary = "Close an account",
               description = "Close an account if;<br>"
               + "0. Request JSON is not malformed (contains both clientUsername and accountNumber, no error on syntax/type, etc)<br>"
               + "1. There is a client with the provided clientUsername<br>"
               + "2. and also there is an account with the provided accountNumber<br>"
               + "3. and the account is not closed yet<br>"
               + "4. and the account has zero balance<br>"
               + "5. and the account is owned by the client<br><br>"
               + "There are some pre-made clients and accounts for demo purposes.<br />"
               + "clientUsername: \"clientWithoutAccount\", \"clientWithOneAccount\", \"clientWithTenAccount\"<br />"
               + "accountNumber: from \"1000000000\" to \"1000000010\"<br />"
               + "NOTE1: the first account is owned by \"clientWithOneAccount\" and the rest are owned by \"clientWithTenAccount\"<br />"
               + "NOTE2: accounts between 1000000000 and 1000000005 have non zero balance whereas accounts between 1000000006 and 1000000010 have zero balance<br />")
    @DeleteMapping(value = "api/account", produces = "application/json")
    public CloseAccountResponse closeAccount(
        @RequestBody @Valid CloseAccountRequest request) {
        return accountService.closeAccount(request);
    }

    @Operation(summary = "Retrieve a list of accounts belong to the client",
               description = "Retrieve a list of accounts belong to the client if there is a client with the provided clientUsername")
    @GetMapping(value = "api/account/{clientUsername}", produces = "application/json")
    public ListAccountResponse listAccount(
        @Parameter(required=true, example = "clientWithTenAccount") 
        @PathVariable String clientUsername) {
        return accountService.listAccount(clientUsername);
    }
}
