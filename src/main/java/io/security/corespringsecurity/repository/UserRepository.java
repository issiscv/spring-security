package io.security.corespringsecurity.repository;

import io.security.corespringsecurity.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);
}
