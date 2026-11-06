package com.devaryan.minibank.repository;

import com.devaryan.minibank.entity.TransactionEntity;
import com.devaryan.minibank.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Page<TransactionEntity> findByAccountNumberOrderByCreatedAtDescIdDesc(String accountNumber, Pageable pageable);

    long countByAccountNumberAndTypeAndCreatedAtAfter(String accountNumber, TransactionType type, Instant after);

    boolean existsByAccountNumberAndTypeAndCounterpartyAccountNumber(String accountNumber, TransactionType type,
                                                                     String counterpartyAccountNumber);

    @Query("select avg(t.amount) from TransactionEntity t where t.accountNumber = :accountNumber and t.type = :type")
    Double averageAmount(@Param("accountNumber") String accountNumber, @Param("type") TransactionType type);
}
