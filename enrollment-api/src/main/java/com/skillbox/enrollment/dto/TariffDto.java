package com.skillbox.enrollment.dto;

import java.math.BigDecimal;

public record TariffDto(
    Long id,
    String name,
    BigDecimal price
) {}