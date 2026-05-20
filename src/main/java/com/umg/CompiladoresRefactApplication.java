package com.umg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CompiladoresRefactApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompiladoresRefactApplication.class, args);
    }
}
