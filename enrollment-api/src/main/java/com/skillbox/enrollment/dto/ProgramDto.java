package com.skillbox.enrollment.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProgramDto(
    Long id,
    String title,
    String description,
    String openEdxCourseId,
    List<TariffDto> tariffs
) {}