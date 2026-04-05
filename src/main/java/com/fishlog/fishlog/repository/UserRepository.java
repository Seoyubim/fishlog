package com.fishlog.fishlog.repository;

import com.fishlog.fishlog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
