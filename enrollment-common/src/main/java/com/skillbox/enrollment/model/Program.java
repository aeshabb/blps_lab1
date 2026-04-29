package com.skillbox.enrollment.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "programs")
@Data
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String openEdxCourseId;
}
