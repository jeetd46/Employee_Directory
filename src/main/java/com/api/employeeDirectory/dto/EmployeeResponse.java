package com.api.employeeDirectory.dto;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeResponse(

        Long id,
        String name,
        String email,
        String designation,
        LocalDate joiningDate,
        DepartmentSummary department,
        Set<SkillResponse> skills
) {
    public record DepartmentSummary(Long id, String name) {}
}
