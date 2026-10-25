package com.example.librarymgnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.librarymgnt.entity.Borrow;
import com.example.librarymgnt.entity.BorrowStatus;

public interface BorrowRepository extends JpaRepository<Borrow, Integer> {
	List<Borrow> findByUserUserid(Integer userid);

	// check if this user already has this book and has not returned it
	@Query("select count(b) > 0 from Borrow b where b.user.userid = :userid and b.book.book_id = :bookId and b.status = :status")
	boolean existsActiveBorrow(@Param("userid") Integer userid, @Param("bookId") Integer bookId, @Param("status") BorrowStatus status);
}