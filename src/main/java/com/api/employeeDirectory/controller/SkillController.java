package com.api.employeeDirectory.controller;

import com.api.employeeDirectory.dto.SkillRequest;
import com.api.employeeDirectory.dto.SkillResponse;
import com.api.employeeDirectory.services.SkillService;
import com.api.employeeDirectory.services.SkillServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillServiceImpl skillService;

    public SkillController(SkillServiceImpl skillService) { this.skillService = skillService; }

    @PostMapping
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        return new ResponseEntity<>(skillService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SkillResponse>> findAll() {
        return new ResponseEntity<>(skillService.findAll(), HttpStatus.OK);
    }
}
