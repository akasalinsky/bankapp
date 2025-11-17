package com.example.cash_service.controller;

import com.example.cash_service.model.CashRequest;
import com.example.cash_service.service.CashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Autowired
    private CashService cashService;

    @PostMapping("/user/{login}/operations")
    public ResponseEntity<?> handleCashOperation(
            @PathVariable String login,
            @RequestBody CashRequest request,
            Authentication authentication
            ) {

        String jwtToken = null;
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            jwtToken = jwt.getTokenValue();
            System.out.println("jwtToken: " + jwtToken);
        }

        if (jwtToken == null) {
            return ResponseEntity.status(401).body("JWT token not found in Authentication context.");
        }

        try {
            if ("PUT".equals(request.getOperationType())) {
                cashService.deposit(login, request, jwtToken);
                return ResponseEntity.ok().build();
            } else if ("GET".equals(request.getOperationType())) {
                cashService.withdraw(login, request, jwtToken);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("Invalid operation type");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody CashRequest request){
        return ResponseEntity.ok(request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody CashRequest request) {
        return ResponseEntity.ok(request);
    }

}
