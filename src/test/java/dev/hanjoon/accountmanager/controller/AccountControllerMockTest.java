package dev.hanjoon.accountmanager.controller;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import dev.hanjoon.accountmanager.dto.AccountNumberAndBalance;
import dev.hanjoon.accountmanager.dto.CloseAccountResponse;
import dev.hanjoon.accountmanager.dto.CreateAccountResponse;
import dev.hanjoon.accountmanager.dto.ListAccountResponse;
import dev.hanjoon.accountmanager.service.AccountService;
import dev.hanjoon.accountmanager.service.TransactionService;

@WebMvcTest(AccountController.class)
public class AccountControllerMockTest {
    @MockBean
    private AccountService accountService;
    @MockBean
    private TransactionService transactionService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccountControllerTest() throws Exception {
        String clientUsername = "createAccountControllerTest", accountNumber = "9876543210";
        BDDMockito.given(accountService.createAccount(any()))
                  .willReturn(new CreateAccountResponse(clientUsername, accountNumber, LocalDateTime.now()));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account")
                                              .contentType("application/json")
                                              .content("{\"clientUsername\":\"" + clientUsername
                                                       + "\", \"initialBalance\":100}"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.clientUsername").value(clientUsername))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accountNumber").value(accountNumber))
               .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
               .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void closeAccountControllerTest() throws Exception {
        String clientUsername = "closeAccountControllerTest", accountNumber = "3333333333";
        BDDMockito.given(accountService.closeAccount(any()))
                  .willReturn(CloseAccountResponse.builder()
                                .clientUsername(clientUsername)
                                .accountNumber(accountNumber)
                                .closedAt(LocalDateTime.now())
                                .build());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/account/")
                                              .contentType("application/json")
                                              .content("{\"clientUsername\":\"" + clientUsername 
                                                       + "\", \"accountNumber\":\"" + accountNumber + "\"}"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.clientUsername").value(clientUsername))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accountNumber").value(accountNumber))
               .andExpect(MockMvcResultMatchers.jsonPath("$.closedAt").isNotEmpty())
               .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void listAccountControllerTest() throws Exception {
        String clientUsername = "listAccountControllerTest";
        BDDMockito.given(accountService.listAccount(clientUsername))
                  .willReturn(ListAccountResponse.builder()
                                .clientUsername(clientUsername)
                                .accounts(new ArrayList<AccountNumberAndBalance>() {{
                                    add(new AccountNumberAndBalance("1111111111", 100L));
                                    add(new AccountNumberAndBalance("2222222222", 200L));}})
                                .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/account/" + clientUsername))
               .andExpect(MockMvcResultMatchers.jsonPath("$.clientUsername").value(clientUsername))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[0].accountNumber").value("1111111111"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[1].accountNumber").value("2222222222"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[0].balance").value(100L))
               .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[1].balance").value(200L))
               .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
