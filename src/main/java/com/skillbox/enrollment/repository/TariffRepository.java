package com.skillbox.enrollment.repository;

import com.skillbox.enrollment.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findByProgramId(Long programId);
}