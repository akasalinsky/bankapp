package com.example.transfer_service.controller;

import com.example.transfer_service.model.TransferRequest;
import com.example.transfer_service.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/internal")
    public ResponseEntity<?> internalTransfer(@RequestBody TransferRequest request) {
        try {
            transferService.internalTransfer(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Transfer failed: " + e.getMessage());
        }
    }

    @PostMapping("/external")
    public ResponseEntity<?> externalTransfer(@RequestBody TransferRequest request) {
        try {
            transferService.externalTransfer(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Transfer failed: " + e.getMessage());
        }
    }
}