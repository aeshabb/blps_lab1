package com.skillbox.enrollment.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProgramDto(
    Long id,
    String title,
    String description,
    List<TariffDto> tariffs
) {}