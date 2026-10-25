package com.api.employeeDirectory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Department")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String location;

    @Builder.Default
    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new ArrayList<>();

}
