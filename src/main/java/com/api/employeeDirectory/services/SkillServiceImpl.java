package com.api.employeeDirectory.services;

import com.api.employeeDirectory.dto.SkillRequest;
import com.api.employeeDirectory.dto.SkillResponse;
import com.api.employeeDirectory.entity.Skill;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.repository.SkillRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillResponse create(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A skill with this name already exists");
        }
        return toResponse(skillRepository.save(Skill.builder()
                .name(request.name().trim())
                .category(trimOptionalField(request.category()))
                .build()));
    }

    @Override
    public List<SkillResponse> findAll() {
        return skillRepository.findAll().stream().map(this::toResponse).toList();
    }


    //------------------ Helping Methods ------------- //

    // This method searches and indicates skillId existence in the "Skill" table
//    public Skill getSkill(Long id) {
//        return skillRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
//    }

    // This method is to build a structured Skill response
    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory());
    }

    // trimming the fields that can be null and avoiding NullPointerException
    private String trimOptionalField(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
