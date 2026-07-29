package com.attendenceSystem.module.attendance.repository;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

import org.springframework.data.jpa.domain.Specification;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;

import jakarta.persistence.criteria.JoinType;

public class LeaveRequestSpecification {

    public static Specification<LeaveRequest> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.join("user", JoinType.INNER).get("fullName")), pattern);
        };
    }

    public static Specification<LeaveRequest> hasStatus(LeaveStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<LeaveRequest> hasWeek(String week) {
        return (root, query, cb) -> {
            if (week == null || week.isBlank()) {
                return cb.conjunction();
            }
            try {
                // Parse week string like "2026-W30"
                int year = Integer.parseInt(week.substring(0, 4));
                int weekNumber = Integer.parseInt(week.substring(6));

                // Get the Monday of the given ISO week
                LocalDate weekStart = LocalDate.of(year, 1, 1)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
                        .with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);

                // Find leave requests that overlap with this week
                // i.e. startDate <= weekEnd AND endDate >= weekStart
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("startDate"), weekEnd),
                        cb.greaterThanOrEqualTo(root.get("endDate"), weekStart));
            } catch (Exception e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<LeaveRequest> hasDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            boolean hasStart = startDate != null;
            boolean hasEnd = endDate != null;

            if (!hasStart && !hasEnd) {
                return cb.conjunction();
            }

            if (hasStart && hasEnd) {
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("startDate"), startDate),
                        cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (hasStart) {
                return cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
            }

            return cb.lessThanOrEqualTo(root.get("endDate"), endDate);
        };
    }
}