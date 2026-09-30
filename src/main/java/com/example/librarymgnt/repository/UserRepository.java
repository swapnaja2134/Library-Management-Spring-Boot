package com.example.librarymgnt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librarymgnt.entity.Role;
import com.example.librarymgnt.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);
	User findByUsername(String username);
	Optional<User> findOptionalByEmail(String email);
	Optional<User> findOptionalByUsername(String username);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
	List<User> findByRole(Role role);
}
