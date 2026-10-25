package com.api.employeeDirectory.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeRequest(
        @NotBlank(message = "Employee name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        String designation,

        @NotNull(message = "Joining date is required")
        @PastOrPresent(message = "Joining date cannot be in the future")
        LocalDate joiningDate,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        Set<Long> skillIds
) {}
