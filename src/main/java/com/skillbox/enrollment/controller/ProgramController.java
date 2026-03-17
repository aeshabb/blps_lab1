package com.skillbox.enrollment.controller;

import com.skillbox.enrollment.dto.ProgramDto;
import com.skillbox.enrollment.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    public List<ProgramDto> getPrograms() {
        return programService.getAllPrograms();
    }
}