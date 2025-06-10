package com.pthieu.identity_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pthieu.identity_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Additional query methods can be defined here if needed
    
    boolean existsByUsername(String Username);
}
