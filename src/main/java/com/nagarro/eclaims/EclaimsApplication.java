package com.nagarro.eclaims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableAspectJAutoProxy
public class EclaimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EclaimsApplication.class, args);
    }

}
