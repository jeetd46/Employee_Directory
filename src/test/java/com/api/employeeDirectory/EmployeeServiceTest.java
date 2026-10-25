package com.api.employeeDirectory;


import com.api.employeeDirectory.config.DirectoryProperties;
import com.api.employeeDirectory.config.PaginationProperties;
import com.api.employeeDirectory.dto.*;
import com.api.employeeDirectory.entity.Department;
import com.api.employeeDirectory.entity.Employee;
import com.api.employeeDirectory.entity.Skill;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.exception.ResourceNotFoundException;
import com.api.employeeDirectory.repository.*;
import com.api.employeeDirectory.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    SkillRepository skillRepository;
    @Mock
    DirectoryProperties directoryProperties;

    @Mock
    DepartmentServiceImpl departmentService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private Employee employee;
    private Skill skill1;
    private Skill skill2;
    private Skill skill3;
    EmployeeRequest employeeRequest;


    @BeforeEach
    void setUp() {
        employeeRequest = new EmployeeRequest("Ram", "ram@gmail.com", "Analyst",
                LocalDate.of(2026, 5, 8), 1L, Set.of(1L, 2L));

        skill1 = Skill.builder().id(1L).name("Java").category("Backend").build();
        skill2 = Skill.builder().id(2L).name("SQL").category("Database").build();
        skill3 = Skill.builder().id(3L).name("AWS").category("Cloud").build();

        department = Department.builder()
                .id(1L).name("Development").location("Kolkata").build();

        employee = Employee.builder().id(1L).name("Shyam").email("shyam@gmail.com")
                .designation("Engineer").joiningDate(LocalDate.of(2025, 5, 8))
                .department(department).skills(new HashSet<>(Set.of(skill1, skill2, skill3))).build();
    }

    // -------Creat----------- //

    @Test
    void add_EmployeeWithNoSkills() {
        when(employeeRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(departmentService.getDepartment(any())).thenReturn(department);
        when(directoryProperties.getMaxSkillsPerEmployee()).thenReturn(10);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.<Employee>getArgument(0));

        EmployeeRequest request = new EmployeeRequest("Rohit", "rohit@gmail.com", "Engineer",
                LocalDate.of(2024, 1, 10), 1L, Set.of());
        EmployeeResponse response = employeeService.create(request);

        assertEquals("Rohit", response.name());
        assertEquals("rohit@gmail.com", response.email());
        assertEquals("Development", response.department().name());
        assertTrue(response.skills().isEmpty());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void add_RejectsDuplicateEmail() {
        when(employeeRepository.existsByEmailIgnoreCase(any())).thenReturn(true);
        assertThrows(ConflictException.class, () -> employeeService.create(employeeRequest));
        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(departmentService);
    }

    @Test
    void add_RejectsWhenSkillLimitExceeded() {
        when(employeeRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(departmentService.getDepartment(any()))
                .thenReturn(department);
        when(directoryProperties.getMaxSkillsPerEmployee()).thenReturn(1);
        when(skillRepository.findAllById(any())).thenReturn(java.util.List.of(
                skill1, skill2));
        assertThrows(ConflictException.class, () -> employeeService.create(employeeRequest));
        verify(employeeRepository, never()).save(any());
    }


    //---------Find--------------//

    @Test
    void get_EmployeeWithDepartmentSkills() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any()))
                .thenReturn(Optional.ofNullable(employee));

        EmployeeResponse result = employeeService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.department().id());
        assertEquals(3L, result.skills().size());

    }

    @Test
    void get_UnknownEmployeeThrowsException() {

        when(employeeRepository.findWithDepartmentAndSkillsById(any()))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(1L));

    }

    @Test
    void get_AllEmployees() {
        PaginationProperties paginationProperties = new PaginationProperties();
        Pageable pageable = PageRequest.of(0, paginationProperties.getDefaultPageSize(),
                Sort.by(Sort.Direction.DESC,"id") );
        when(employeeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(employee)));

        assertEquals(1,employeeService
                .findAll(null, 0,paginationProperties.getDefaultPageSize(),
                        "id","desc").getContent().size());
    }

    // -------Update----------- //
    @Test
    void update_DepartmentAndReplacesSkills() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.of(employee));
        when(departmentService.getDepartment(any())).thenReturn(Department.builder()
                .id(1L).name("Testing").location("Kolkata").build());
        when(skillRepository.findAllById(any())).thenReturn(
                List.of(Skill.builder().id(1L).name("Python")
                        .category("Backend").build(),
                        Skill.builder().id(1L)
                                .name("PostgreSQL")
                                .category("Database").build()));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(directoryProperties.getMaxSkillsPerEmployee()).thenReturn(10);

        EmployeeResponse result = employeeService.update(1L, employeeRequest);

        assertEquals("Testing", result.department().name());
        assertEquals("ram@gmail.com", result.email());
        assertEquals(2L, result.skills().size());
    }

    @Test
    void update_RejectDuplicateEmail() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> employeeService
                .update(6L, employeeRequest));
        verify(employeeRepository, never()).save(any());
    }

    // -------Delete----------- //

    @Test
    void delete_ExistingEmployee() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.of(employee));
        employeeService.delete(4L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void delete_RejectForUnknownEmployee() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(5L));
    }

    // ---------- add / remove skill ----------

    @Test
    void add_SkillWhenWithinLimit() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any()))
                .thenReturn(Optional.of(employee));
        when(skillRepository.findById(any())).thenReturn(Optional.of(
                Skill.builder().id(4L).name("Python").category("Backend").build()
        ));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.<Employee>getArgument(0));
        when(directoryProperties.getMaxSkillsPerEmployee()).thenReturn(10);

        EmployeeResponse result = employeeService.addSkill(1L, 6L);

        assertEquals(4L, result.skills().size() );
        assertTrue(result.skills().stream().anyMatch(n->n.name().equals("Python")) );
        assertTrue(result.skills().stream().anyMatch(n->n.category().equals("Backend")));
    }


    @Test
    void add_UnknownSkillsThrowsException() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.of(employee));
        when(skillRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.addSkill(1L, 7L));
    }

    @Test
    void delete_SkillOfEmployee() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any())).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(skillRepository.findById(any())).thenReturn(Optional.ofNullable(skill1));

        EmployeeResponse result = employeeService.removeSkill(1L, 2L);

        assertFalse(result.skills().stream().anyMatch(n->n.id() == 2L));
        assertFalse(result.skills().stream().anyMatch(n->n.name() == "SQL"));
    }

    @Test
    void delete_UnknownSkillOfEmployeeThrowsException() {
        when(employeeRepository.findWithDepartmentAndSkillsById(any()))
                .thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class, () -> employeeService.removeSkill(1L, 4L));
        verify(employeeRepository, never()).save(any());
    }


}
