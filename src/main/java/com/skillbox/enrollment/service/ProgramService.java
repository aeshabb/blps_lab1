package com.skillbox.enrollment.service;

import com.skillbox.enrollment.dto.ProgramDto;
import com.skillbox.enrollment.dto.TariffDto;
import com.skillbox.enrollment.model.Program;
import com.skillbox.enrollment.model.Tariff;
import com.skillbox.enrollment.repository.ProgramRepository;
import com.skillbox.enrollment.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {
    
    private final ProgramRepository programRepository;
    private final TariffRepository tariffRepository;

    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ProgramDto mapToDto(Program program) {
        List<TariffDto> tariffs = tariffRepository.findByProgramId(program.getId())
                .stream()
                .map(t -> new TariffDto(t.getId(), t.getName(), t.getPrice()))
                .collect(Collectors.toList());
        return new ProgramDto(program.getId(), program.getTitle(), program.getDescription(), tariffs);
    }
}