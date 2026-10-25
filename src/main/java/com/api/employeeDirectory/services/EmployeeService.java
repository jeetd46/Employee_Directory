package com.api.employeeDirectory.services;

import com.api.employeeDirectory.dto.EmployeeRequest;
import com.api.employeeDirectory.dto.EmployeeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> findByDepartment(Long id);

    EmployeeResponse create(@Valid EmployeeRequest request);

    Page<EmployeeResponse> findAll(Long departmentId, int page, Integer size, String sortBy, String sortDir);

    EmployeeResponse findById(Long id);

    EmployeeResponse update(Long id, @Valid EmployeeRequest request);

    String delete(Long id);

    EmployeeResponse addSkill(Long id, Long sid);

    EmployeeResponse removeSkill(Long id, Long sid);

}
