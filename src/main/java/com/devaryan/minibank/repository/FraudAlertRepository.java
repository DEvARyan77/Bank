package com.devaryan.minibank.repository;

import com.devaryan.minibank.entity.FraudAlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, Long> {

    Page<FraudAlertEntity> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
}
