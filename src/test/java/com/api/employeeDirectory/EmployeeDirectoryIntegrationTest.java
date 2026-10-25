package com.api.employeeDirectory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeDirectoryIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void infoEndpointReturnsConfiguredValues() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Employee Directory API"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void departmentCanBeCreatedAndListed() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Testing\",\"location\":\"Kolkata\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Testing"))
                .andExpect(jsonPath("$.headCount").value(0));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidDepartmentRequestReturnsStructuredError() throws Exception {
        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Department not found with id: "+1))
                .andExpect(jsonPath("$.path").value("/api/departments/1"));
    }
}
