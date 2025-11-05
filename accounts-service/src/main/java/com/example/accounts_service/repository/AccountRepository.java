package com.example.accounts_service.repository;

import com.example.accounts_service.model.Account;
import com.example.accounts_service.model.Currency;
import com.example.accounts_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByUserAndCurrency(User user, Currency currency);
    boolean existsByUserAndCurrency(User user, Currency currency);


    //List<Account> findByLogin(String login);
    //boolean existsByUserId(Long userId);      // ✅ Для проверки существования

}
