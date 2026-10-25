package com.api.employeeDirectory.controller;


import com.api.employeeDirectory.dto.DepartmentRequest;
import com.api.employeeDirectory.dto.DepartmentResponse;
import com.api.employeeDirectory.dto.EmployeeResponse;
import com.api.employeeDirectory.services.DepartmentService;
import com.api.employeeDirectory.services.DepartmentServiceImpl;
import com.api.employeeDirectory.services.EmployeeService;
import com.api.employeeDirectory.services.EmployeeServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentServiceImpl departmentService;
    private final EmployeeServiceImpl employeeService;

    public DepartmentController(DepartmentServiceImpl departmentService,
                                EmployeeServiceImpl employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return new ResponseEntity<>(departmentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return new ResponseEntity<>(departmentService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> findById(@PathVariable Long id) {
        return new ResponseEntity<>(departmentService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return new ResponseEntity<>(departmentService.update(id, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        return new ResponseEntity<>(departmentService.delete(id), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/employees")
    public List<EmployeeResponse> findEmployees(@PathVariable Long id) {
        return employeeService.findByDepartment(id);
    }
}

