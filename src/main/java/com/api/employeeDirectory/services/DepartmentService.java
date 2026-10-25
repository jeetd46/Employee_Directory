package com.api.employeeDirectory.services;

import com.api.employeeDirectory.dto.DepartmentRequest;
import com.api.employeeDirectory.dto.DepartmentResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(@Valid DepartmentRequest request);

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Long id);

    DepartmentResponse update(Long id, @Valid DepartmentRequest request);

    String delete(Long id);
}
