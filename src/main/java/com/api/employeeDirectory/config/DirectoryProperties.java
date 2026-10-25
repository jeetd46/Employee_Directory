package com.api.employeeDirectory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "directory")
public class DirectoryProperties {
    private int maxSkillsPerEmployee = 10;

    public int getMaxSkillsPerEmployee() { return maxSkillsPerEmployee; }
    public void setMaxSkillsPerEmployee(int maxSkillsPerEmployee) {
        this.maxSkillsPerEmployee = maxSkillsPerEmployee;
    }
}
