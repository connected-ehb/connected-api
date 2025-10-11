package com.ehb.connected.domain.impl.dashboard.service;

import com.ehb.connected.domain.impl.assignments.dto.DashboardDetailsDto;

public interface DashboardService {
    DashboardDetailsDto getDashboardDetails(Long assignmentId);
}
