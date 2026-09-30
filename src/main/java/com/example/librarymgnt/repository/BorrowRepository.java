package com.example.librarymgnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librarymgnt.entity.Borrow;

public interface BorrowRepository extends JpaRepository<Borrow, Integer> {
	List<Borrow> findByUserUserid(Integer userid);
}
