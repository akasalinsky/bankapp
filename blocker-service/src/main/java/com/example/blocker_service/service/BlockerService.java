package com.example.blocker_service.service;

import com.example.blocker_service.model.BlockedOperation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Random;

@Service
public class BlockerService {

    private final Random random = new Random();

    /**
     * Проверяет, нужно ли блокировать операцию
     * @param accountId ID счета
     * @param amount Сумма операции
     * @param currency Валюта
     * @param operationType Тип операции (DEPOSIT, WITHDRAW, TRANSFER)
     * @return true если операция заблокирована, false если разрешена
     */
    public boolean shouldBlockOperation(Long accountId, BigDecimal amount,
                                        String currency, String operationType) {

        // 1. Проверка на подозрительную сумму
        if (isSuspiciousAmount(amount)) {
            return true;
        }

        // 2. Проверка на ночное время
        if (isNightTime()) {
            return true;
        }

        // 3. Случайная блокировка (имитация подозрительной активности)
        if (random.nextInt(100) < 5) { // 5% шанс блокировки
            return true;
        }

        // 4. Проверка на подозрительный тип операции
        if (isSuspiciousOperation(operationType)) {
            return true;
        }

        return false; // Операция разрешена
    }

    /**
     * Создает запись о заблокированной операции
     */
    public BlockedOperation createBlockedOperation(Long accountId, BigDecimal amount,
                                                   String currency, String operationType,
                                                   String reason) {
        return new BlockedOperation(accountId, amount, currency, operationType, reason);
    }

    /**
     * Проверяет, является ли сумма подозрительной
     */
    private boolean isSuspiciousAmount(BigDecimal amount) {
        // Блокируем операции больше 1000000 в любой валюте
        return amount.compareTo(new BigDecimal("1000000")) > 0;
    }

    /**
     * Проверяет, происходит ли операция ночью
     */
    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        // Блокируем операции с 23:00 до 06:00
        return now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(6, 0));
    }

    /**
     * Проверяет, является ли операция подозрительной
     */
    private boolean isSuspiciousOperation(String operationType) {
        // Блокируем операции с неизвестным типом
        return operationType == null ||
                (!"DEPOSIT".equals(operationType) &&
                        !"WITHDRAW".equals(operationType) &&
                        !"TRANSFER".equals(operationType));
    }

    /**
     * Получает причину блокировки
     */
    public String getBlockReason(Long accountId, BigDecimal amount,
                                 String currency, String operationType) {
        if (isSuspiciousAmount(amount)) {
            return "Сумма операции превышает лимит";
        }

        if (isNightTime()) {
            return "Операция в ночное время";
        }

        if (random.nextInt(100) < 5) {
            return "Подозрительная активность";
        }

        if (isSuspiciousOperation(operationType)) {
            return "Недопустимый тип операции";
        }

        return "Неизвестная причина";
    }
}