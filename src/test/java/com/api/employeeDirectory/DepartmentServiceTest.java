package com.api.employeeDirectory;

import com.api.employeeDirectory.dto.DepartmentRequest;
import com.api.employeeDirectory.dto.DepartmentResponse;
import com.api.employeeDirectory.entity.Department;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.exception.ResourceNotFoundException;
import com.api.employeeDirectory.repository.DepartmentRepository;
import com.api.employeeDirectory.repository.EmployeeRepository;
import com.api.employeeDirectory.services.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    public DepartmentServiceImpl departmentService;

    private DepartmentRequest departmentRequest;
    private Department department;



    @BeforeEach
    public void setUp(){

        departmentRequest = new DepartmentRequest("Development", "Kolkata");
        department =
            Department.builder().id(1L).name("Testing").location("Kolkata").build();
    }


    // -------Creat----------- //

    @Test
    public void add_DepartmentWithoutLocation(){
        when(departmentRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DepartmentRequest departmentRequest = new DepartmentRequest("HR","");
        DepartmentResponse response = departmentService.create(departmentRequest);
        assertTrue(response.name().equalsIgnoreCase("HR"));
        assertNull(response.location());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    public void add_RejectDuplicateDepartment() {
        when(departmentRepository.existsByNameIgnoreCase(any())).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> departmentService.create(departmentRequest));
        verify(departmentRepository, never()).save(any());
    }

    // -------Find----------- //

    @Test
    void get_DepartmentWithComputedHeadCount() {
        when(departmentRepository.findById(any())).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(any())).thenReturn(2L);

        DepartmentResponse result = departmentService.findById(6L);

        assertEquals(2L, result.headCount());
        assertEquals("Testing", result.name());
    }

    @Test
    void get_ExceptionForUnknownId() {
        when(departmentRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.findById(9L));
    }


    // -------Update----------- //

    @Test
    void update_DepartmentFields() {
        when(departmentRepository.findById(any())).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.countByDepartmentId(any())).thenReturn(2L);

        DepartmentResponse result = departmentService
                .update(1L, departmentRequest);

        assertEquals("Development",result.name());
        assertEquals("Kolkata", result.location());
        assertEquals(2L,result.headCount());
    }

    @Test
    public void update_RejectWithDuplicateDepartmentName(){
        when(departmentRepository.findById(any())).thenReturn(Optional.ofNullable(department));
        when(departmentRepository.existsByNameIgnoreCase(any())).thenReturn(true);
        assertThrows(ConflictException.class, ()->departmentService
                .update(1L, departmentRequest));
    }


    // -------Delete----------- //

    @Test
    void delete_ExistingDepartment() {
        when(departmentRepository.findById(any())).thenReturn(Optional.of(department));
        when(employeeRepository.existsByDepartmentId(any())).thenReturn(false);

        departmentService.delete(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void delete_RejectOfDepartmentWithExistingEmployees() {
        when(departmentRepository.findById(any())).thenReturn(Optional.of(department));
        when(employeeRepository.existsByDepartmentId(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> departmentService.delete(1L));
        verify(departmentRepository, never()).delete(any());
    }



}
