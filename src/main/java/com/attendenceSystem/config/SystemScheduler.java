package com.attendenceSystem.config;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.attendenceSystem.module.attendance.service.AttendanceScheduleService;
import com.attendenceSystem.module.attendance.service.LeaveScheduleService;
import com.attendenceSystem.module.system.entity.SystemSetting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduler implements SchedulingConfigurer {

    private static final String DEFAULT_AUTO_MARK_ABSENT_CRON = "0 0 10 * * *";
    private static final String DEFAULT_AUTO_HANDLE_MISSING_CHECKOUT_CRON = "0 50 23 * * *";
    private static final String DEFAULT_LEAVE_AUTO_REJECT_CRON = "0 0 22 * * *";

    private final SystemConfig systemConfig;
    private final AttendanceScheduleService attendanceScheduleService;
    private final LeaveScheduleService leaveScheduleService;
    private final ZoneId applicationZoneId;

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskScheduler.setPoolSize(3);
        taskScheduler.setThreadNamePrefix("system-scheduler-");
        taskScheduler.initialize();
        taskRegistrar.setScheduler(taskScheduler);
        scheduleAll();
    }

    @EventListener
    public synchronized void onSettingsReloaded(SettingsReloadedEvent event) {
        log.info("Settings reloaded - rescheduling all system jobs.");
        rescheduleAll();
    }

    public synchronized void rescheduleAll() {
        scheduledFutures.forEach(future -> future.cancel(false));
        scheduledFutures.clear();
        scheduleAll();
    }

    private void scheduleAll() {
        SystemSetting settings = systemConfig.get();

        scheduleTask("autoMarkAbsent",
                attendanceScheduleService::autoMarkAbsent,
                settings.getAutoMarkAbsentCron(),
                DEFAULT_AUTO_MARK_ABSENT_CRON);

        scheduleTask("autoHandleMissingCheckOut",
                attendanceScheduleService::autoHandleMissingCheckOut,
                settings.getAutoHandleMissingCheckoutCron(),
                DEFAULT_AUTO_HANDLE_MISSING_CHECKOUT_CRON);

        scheduleTask("autoRejectExpiredLeaveRequests",
                leaveScheduleService::autoRejectExpiredLeaveRequests,
                settings.getLeaveAutoRejectCron(),
                DEFAULT_LEAVE_AUTO_REJECT_CRON);
    }

    private void scheduleTask(String name, Runnable task, String cron, String defaultCron) {
        String cronExpr = (cron != null && !cron.isBlank()) ? cron : defaultCron;
        try {
            CronTrigger trigger = new CronTrigger(cronExpr, applicationZoneId);
            ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
            scheduledFutures.add(future);
            log.info("Scheduled task '{}' with cron '{}' (zone={})", name, cronExpr, applicationZoneId);
        } catch (Exception e) {
            log.error("Failed to schedule task '{}' with cron '{}'", name, cronExpr, e);
        }
    }
}