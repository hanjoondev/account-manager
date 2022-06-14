package dev.hanjoon.accountmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication
@EnableWebMvc
public class AccountManagerApp {
    public static void main(String[] args) {
        SpringApplication.run(AccountManagerApp.class, args);
    }
}
