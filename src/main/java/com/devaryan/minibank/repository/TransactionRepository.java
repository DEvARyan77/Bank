package com.devaryan.minibank.repository;

import com.devaryan.minibank.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Page<TransactionEntity> findByAccountNumberOrderByCreatedAtDescIdDesc(String accountNumber, Pageable pageable);
}
