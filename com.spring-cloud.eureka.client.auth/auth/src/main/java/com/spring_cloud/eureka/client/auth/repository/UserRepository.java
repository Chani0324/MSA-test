package com.spring_cloud.eureka.client.auth.repository;

import com.spring_cloud.eureka.client.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
