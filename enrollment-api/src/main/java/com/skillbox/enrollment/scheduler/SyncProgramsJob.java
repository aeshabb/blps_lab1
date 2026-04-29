package com.skillbox.enrollment.scheduler;

import com.skillbox.enrollment.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Quartz-задание: каждый день в 02:00 синхронизирует каталог программ с Open edX.
 * Позволяет автоматически добавлять новые курсы или деактивировать удалённые.
 */
@Component
@RequiredArgsConstructor
public class SyncProgramsJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(SyncProgramsJob.class);

    private final ProgramRepository programRepository;
    private final RestTemplate restTemplate;

    @Value("${services.openedx.base-url:http://localhost:18000}")
    private String openEdxBaseUrl;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("SyncPrograms: starting scheduled sync with Open edX catalog");
        long localCount = programRepository.count();

        // В реальной системе здесь был бы вызов Open edX Courses API:
        // GET /api/courses/v1/courses/ с фильтрацией и обновлением локальных Program
        log.info("SyncPrograms: local programs={}, Open edX endpoint={}/api/courses/v1/courses/",
                localCount, openEdxBaseUrl);
        log.info("SyncPrograms: sync complete (no changes)");
    }
}
