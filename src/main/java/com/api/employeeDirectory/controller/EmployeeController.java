package com.api.employeeDirectory.controller;

import com.api.employeeDirectory.dto.EmployeeRequest;
import com.api.employeeDirectory.dto.EmployeeResponse;
import com.api.employeeDirectory.services.EmployeeServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeServiceImpl employeeService;

    public EmployeeController(EmployeeServiceImpl employeeService) { this.employeeService = employeeService; }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return new ResponseEntity<>(employeeService.create(request),HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> findAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return new ResponseEntity<>(employeeService.findAll(departmentId, page, size, sortBy, sortDir)
        ,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {

        return new ResponseEntity<>(employeeService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return new ResponseEntity<>(employeeService.update(id, request), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        return new ResponseEntity<>(employeeService.delete(id), HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/skills/{sid}")
    public ResponseEntity<EmployeeResponse> addSkill(@PathVariable Long id, @PathVariable Long sid) {
        return new ResponseEntity<>(employeeService.addSkill(id, sid), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}/skills/{sid}")
    public ResponseEntity<EmployeeResponse> removeSkill(@PathVariable Long id, @PathVariable Long sid) {
        return new ResponseEntity<>(employeeService.removeSkill(id, sid), HttpStatus.OK);
    }
}
