package com.api.employeeDirectory.services;

import com.api.employeeDirectory.dto.SkillRequest;
import com.api.employeeDirectory.dto.SkillResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface SkillService {


    SkillResponse create(@Valid SkillRequest request);

    List<SkillResponse> findAll();
}
