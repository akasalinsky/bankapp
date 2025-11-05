package com.example.exchange_service.repository;

import com.example.exchange_service.model.Currency;
import com.example.exchange_service.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(Currency from, Currency to);

    List<ExchangeRate> findAll();

    List<ExchangeRate> findByFromCurrency(Currency fromCurrency);

    List<ExchangeRate> findByToCurrency(Currency toCurrency);

    @Modifying
    @Query("DELETE FROM ExchangeRate e WHERE e.fromCurrency = :from AND e.toCurrency = :to")
    void deleteByFromCurrencyAndToCurrency(Currency from, Currency to);
}