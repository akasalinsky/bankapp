package com.example.blocker_service.controller;

import com.example.blocker_service.model.BlockedOperation;
import com.example.blocker_service.service.BlockerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/blocker")
public class BlockerController {

    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    /**
     * Проверяет, нужно ли блокировать операцию
     */
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkOperation(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String operationType) {

        try {
            boolean shouldBlock = blockerService.shouldBlockOperation(
                    accountId, amount, currency, operationType);

            return ResponseEntity.ok(shouldBlock);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    /**
     * Получает причину блокировки
     */
    @GetMapping("/reason")
    public ResponseEntity<String> getBlockReason(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String operationType) {

        try {
            String reason = blockerService.getBlockReason(
                    accountId, amount, currency, operationType);

            return ResponseEntity.ok(reason);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка определения причины");
        }
    }

    /**
     * Создает запись о заблокированной операции
     */
    @PostMapping("/blocked")
    public ResponseEntity<BlockedOperation> createBlockedOperation(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String operationType,
            @RequestParam String reason) {

        try {
            BlockedOperation blockedOperation = blockerService.createBlockedOperation(
                    accountId, amount, currency, operationType, reason);

            return ResponseEntity.ok(blockedOperation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}