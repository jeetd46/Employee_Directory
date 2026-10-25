package com.api.employeeDirectory.services;

import com.api.employeeDirectory.dto.DepartmentRequest;
import com.api.employeeDirectory.dto.DepartmentResponse;
import com.api.employeeDirectory.entity.Department;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.exception.ResourceNotFoundException;
import com.api.employeeDirectory.repository.DepartmentRepository;
import com.api.employeeDirectory.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService{

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A department with this name already exists");
        }
        Department department = departmentRepository.save(Department.builder()
                .name(request.name().trim())
                .location(trimOptionalField(request.location()))
                .build());
        return toResponse(department);
    }

    @Override
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public DepartmentResponse findById(Long id) {
        return toResponse(getDepartment(id));
    }

    @Override
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        if (departmentRepository.existsByNameIgnoreCase(request.name().trim())
                && !department.getName().equalsIgnoreCase(request.name().trim())) {
            throw new ConflictException("A department with this name already exists");
        }
        department.setName(request.name().trim());
        department.setLocation(trimOptionalField(request.location()));
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public String delete(Long id) {
        Department department = getDepartment(id);
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new ConflictException("Cannot delete a department that still has employees");
        }
        departmentRepository.delete(department);
        return "Department deleted successfully";
    }


    //------------------ Helping Methods ------------- //

    // This method searches and indicates departmentId existence in the "Department" table
    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    // This method is to build a structured Department response
    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getLocation(),
                Math.toIntExact(employeeRepository.countByDepartmentId(department.getId())));
    }

    // trimming the fields that can be null and avoiding NullPointerException
    private String trimOptionalField(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
