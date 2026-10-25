package com.api.employeeDirectory;

import com.api.employeeDirectory.dto.SkillRequest;
import com.api.employeeDirectory.dto.SkillResponse;
import com.api.employeeDirectory.entity.Skill;
import com.api.employeeDirectory.exception.ConflictException;
import com.api.employeeDirectory.repository.SkillRepository;
import com.api.employeeDirectory.services.SkillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    SkillRepository skillRepository;
    @InjectMocks
    SkillServiceImpl skillService;

    private SkillRequest skillRequest ;
    private Skill skill;
    private Skill skill2;

    @BeforeEach
    void setUp(){
        skillRequest =new SkillRequest(" Java ", "Backend");
        skill = Skill.builder().id(1L).name("SQL").category("Database").build();
        skill2 = Skill.builder().id(3L).name("AWS").category("Cloud").build();
    }

    // -------Creat----------- //

    @Test
    void add_uniqueSkills() {
        when(skillRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(skillRepository.save(any(Skill.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SkillResponse result = skillService.create(skillRequest);

        assertEquals("Java", result.name());
        assertEquals("Backend", result.category());
    }

    @Test
    void add_rejectDuplicateSkillCreation() {
        when(skillRepository.existsByNameIgnoreCase(any())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> skillService.create(skillRequest));
        verify(skillRepository, never()).save(any());
    }


    // -------Find----------- //

    @Test
    void get_AllSkills() {
        when(skillRepository.findAll())
                .thenReturn(List.of(skill,skill2));

        assertEquals(2L, skillService.findAll().size());
    }
}
