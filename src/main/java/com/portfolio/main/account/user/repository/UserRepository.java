package com.portfolio.main.account.user.repository;

import com.portfolio.main.account.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Long findIdByLoginId(String loginId);
    Optional<User> findByLoginId(String loginId);
}
