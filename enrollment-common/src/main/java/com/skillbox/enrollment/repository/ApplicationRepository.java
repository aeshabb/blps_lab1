package com.skillbox.enrollment.repository;

import com.skillbox.enrollment.model.Application;
import com.skillbox.enrollment.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStatusAndCreatedAtBefore(ApplicationStatus status, LocalDateTime before);
}
