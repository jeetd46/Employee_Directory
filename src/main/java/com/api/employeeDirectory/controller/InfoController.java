package com.api.employeeDirectory.controller;

import com.api.employeeDirectory.config.AppProperties;
import com.api.employeeDirectory.dto.InfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
public class InfoController {
    private final AppProperties appProperties;

    public InfoController(AppProperties appProperties) { this.appProperties = appProperties; }

    @GetMapping
    public ResponseEntity<InfoResponse> info() {
        return new ResponseEntity<>(new InfoResponse(appProperties.getName(), appProperties.getVersion())
                , HttpStatus.OK);
    }
}
