package com.skillbox.enrollment.config;

import com.skillbox.enrollment.scheduler.ExpireStalePaymentsJob;
import com.skillbox.enrollment.scheduler.SyncProgramsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Quartz Scheduler.
 * Регистрирует два периодических задания:
 * 1. ExpireStalePaymentsJob — каждый час истекает заявки, зависшие в PAYMENT_PENDING > 24ч.
 * 2. SyncProgramsJob       — каждый день в 2:00 синхронизирует каталог курсов с Open edX.
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail expireStalePaymentsJobDetail() {
        return JobBuilder.newJob(ExpireStalePaymentsJob.class)
                .withIdentity("expireStalePaymentsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expireStalePaymentsTrigger(JobDetail expireStalePaymentsJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(expireStalePaymentsJobDetail)
                .withIdentity("expireStalePaymentsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))  // каждый час
                .build();
    }

    @Bean
    public JobDetail syncProgramsJobDetail() {
        return JobBuilder.newJob(SyncProgramsJob.class)
                .withIdentity("syncProgramsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger syncProgramsTrigger(JobDetail syncProgramsJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(syncProgramsJobDetail)
                .withIdentity("syncProgramsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))  // каждый день в 2:00
                .build();
    }
}
