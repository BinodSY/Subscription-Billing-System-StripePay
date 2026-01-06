package com.billing.billingsystem.users.databaseArchitecture;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.billingsystem.users.domain.User;

import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {

}
