package com.api.employeeDirectory.services;

import com.api.employeeDirectory.config.DirectoryProperties;
import com.api.employeeDirectory.config.PaginationProperties;
import com.api.employeeDirectory.dto.EmployeeRequest;
import com.api.employeeDirectory.dto.EmployeeResponse;
import com.api.employeeDirectory.dto.SkillResponse;
import com.api.employeeDirectory.entity.Department;
import com.api.employeeDirectory.entity.Employee;
import com.api.employeeDirectory.entity.Skill;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.exception.ResourceNotFoundException;
import com.api.employeeDirectory.repository.EmployeeRepository;
import com.api.employeeDirectory.repository.SkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



@Service
public class EmployeeServiceImpl implements EmployeeService {


    private final EmployeeRepository employeeRepository;
    private final DepartmentServiceImpl departmentService;
    private final SkillRepository skillRepository;
    private final DirectoryProperties directoryProperties;
    private final PaginationProperties paginationProperties;


    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentServiceImpl departmentService,
                               SkillRepository skillRepository,
                               DirectoryProperties directoryProperties,
                               PaginationProperties paginationProperties) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.skillRepository = skillRepository;
        this.directoryProperties = directoryProperties;
        this.paginationProperties = paginationProperties;
    }

    @Override
    public EmployeeResponse create(EmployeeRequest request) {

        ensureEmailAvailable(request.email(), null);
        Department department = departmentService.getDepartment(request.departmentId());
        Set<Skill> skills = resolveSkills(request.skillIds());
        ensureSkillLimit(skills.size());
        Employee employee = Employee.builder().name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .designation(trimOptionalField(request.designation()))
                .joiningDate(request.joiningDate())
                .department(department)
                .skills(skills)
                .build();
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    public Page<EmployeeResponse> findAll(Long departmentId, int page,
                                          Integer size, String sortBy, String sortDir) {
        if (page < 0) throw new IllegalArgumentException("Page index must be zero or greater");
        int pageSize = size == null ? paginationProperties.getDefaultPageSize() : size;
        if (pageSize < 1 || pageSize > 200) throw new IllegalArgumentException("Page size must be between 1 and 200");
        Set<String> allowedSorts = Set.of("id", "name", "email", "joiningDate");
        if (!allowedSorts.contains(sortBy)) throw new IllegalArgumentException("sortBy must be one of: id, name, email, joiningDate");
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("sortDir must be 'asc' or 'desc'");
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        Page<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findAllByDepartmentId(departmentId, pageable);
        } else {
            employees = employeeRepository.findAll(pageable);
        }
        return employees.map(this::toResponse);
    }

    @Override
    public EmployeeResponse findById(Long id) {
        return toResponse(getEmployee(id));
    }

    @Override
    public List<EmployeeResponse> findByDepartment(Long departmentId) {
        departmentService.getDepartment(departmentId);
        return employeeRepository.findByDepartmentIdOrderByNameAsc(departmentId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployee(id);
        ensureEmailAvailable(request.email(), id);
        Department department = departmentService.getDepartment(request.departmentId());
        Set<Skill> skills = resolveSkills(request.skillIds());
        ensureSkillLimit(skills.size());
        employee.setName(request.name().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setDesignation(trimOptionalField(request.designation()));
        employee.setJoiningDate(request.joiningDate());
        employee.setDepartment(department);
        employee.getSkills().clear();
        employee.getSkills().addAll(skills);
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    public String delete(Long id) {
        employeeRepository.delete(getEmployee(id));
        return "Employee record deleted";
    }

    @Override
    public EmployeeResponse addSkill(Long employeeId, Long skillId) {
        Employee employee = getEmployee(employeeId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        if (employee.getSkills().stream().anyMatch(existing -> existing.getId().equals(skillId))) {
            throw new ConflictException("Employee already has this skill");
        }
        ensureSkillLimit(employee.getSkills().size() + 1);
        employee.getSkills().add(skill);
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse removeSkill(Long employeeId, Long skillId) {
        Employee employee = getEmployee(employeeId);
        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        boolean removed = employee.getSkills().removeIf(skill -> skill.getId().equals(skillId));
        if (!removed) throw new ResourceNotFoundException("Employee does not have skill id: " + skillId);
        return toResponse(employeeRepository.save(employee));
    }

    //------------------ Helping Methods ------------- //

    private Employee getEmployee(Long id) {
        return employeeRepository.findWithDepartmentAndSkillsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }


    // This method is to ensure all the skills that are getting
    // tried to add for an employee are valid as per "Skill" table.
    private Set<Skill> resolveSkills(Set<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return new LinkedHashSet<>();
        Set<Long> uniqueIds = new LinkedHashSet<>(skillIds);
        Set<Skill> skills = new LinkedHashSet<>(skillRepository.findAllById(uniqueIds));
        if (skills.size() != uniqueIds.size()) {
            Set<Long> foundIds = skills.stream().map(Skill::getId).collect(Collectors.toSet());
            uniqueIds.removeAll(foundIds);
            throw new ResourceNotFoundException("Skill IDs not found: " + uniqueIds);
        }
        return skills;
    }

    // This method is to ensure one employee not to exceed specified
    // number of total skills
    private void ensureSkillLimit(int count) {
        int max = directoryProperties.getMaxSkillsPerEmployee() ;
        if (count > max) {
            throw new ConflictException("An employee can have at most " + max + " skills");
        }
    }

    // This method is to avoid email duplication in "Employee" table
    private void ensureEmailAvailable(String email, Long currentEmployeeId) {
        if (employeeRepository.existsByEmailIgnoreCase(email.trim())
                && (currentEmployeeId == null || !employeeRepository.findById(currentEmployeeId)
                .map(employee -> employee.getEmail().equalsIgnoreCase(email.trim())).orElse(false))) {
            throw new ConflictException("An employee with this email already exists");
        }
    }


    // This method is to build a structured Employee response
    private EmployeeResponse toResponse(Employee employee) {
        Set<SkillResponse> skillResponses = employee.getSkills().stream()
                .map(skill -> new SkillResponse(skill.getId(), skill.getName(), skill.getCategory()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new EmployeeResponse(employee.getId(), employee.getName(), employee.getEmail(),
                employee.getDesignation(), employee.getJoiningDate(),
                new EmployeeResponse.DepartmentSummary(employee.getDepartment().getId(), employee.getDepartment().getName()),
                skillResponses);
    }

    // trimming the fields that can be null and avoiding NullPointerException
    private String trimOptionalField(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
