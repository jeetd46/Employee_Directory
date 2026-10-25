package com.api.employeeDirectory.repository;

import com.api.employeeDirectory.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {


    boolean existsByEmailIgnoreCase(String email);
    boolean existsByDepartmentId(Long departmentId);
    long countByDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"department"})
    Page<Employee> findAllByDepartmentId(Long departmentId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"department"})
    Page<Employee> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "skills"})
    Optional<Employee> findWithDepartmentAndSkillsById(Long id);

    @EntityGraph(attributePaths = {"department", "skills"})
    List<Employee> findByDepartmentIdOrderByNameAsc(Long departmentId);
}
