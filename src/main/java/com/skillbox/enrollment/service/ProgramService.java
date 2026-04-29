package com.skillbox.enrollment.service;

import com.skillbox.enrollment.dto.ProgramDto;
import com.skillbox.enrollment.dto.TariffDto;
import com.skillbox.enrollment.model.Program;
import com.skillbox.enrollment.model.Tariff;
import com.skillbox.enrollment.repository.ProgramRepository;
import com.skillbox.enrollment.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProgramService {
    
    private final ProgramRepository programRepository;
    private final TariffRepository tariffRepository;

    @Cacheable(value = "programs")
    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @CacheEvict(value = "programs", allEntries = true)
    public ProgramDto createProgram(ProgramDto dto) {
        Program program = new Program();
        program.setTitle(dto.title());
        program.setDescription(dto.description());
        program.setOpenEdxCourseId(dto.openEdxCourseId());
        
        Program saved = programRepository.save(program);
        
        // create a default tariff
        Tariff tariff = new Tariff();
        tariff.setName("Standard");
        tariff.setPrice(BigDecimal.valueOf(1000));
        tariff.setProgram(saved);
        tariffRepository.save(tariff);
        
        return mapToDto(saved);
    }

    private ProgramDto mapToDto(Program program) {
        List<TariffDto> tariffs = tariffRepository.findByProgramId(program.getId())
                .stream()
                .map(t -> new TariffDto(t.getId(), t.getName(), t.getPrice()))
                .collect(Collectors.toList());
        return new ProgramDto(
            program.getId(),
            program.getTitle(),
            program.getDescription(),
            program.getOpenEdxCourseId(),
            tariffs
        );
    }
}
