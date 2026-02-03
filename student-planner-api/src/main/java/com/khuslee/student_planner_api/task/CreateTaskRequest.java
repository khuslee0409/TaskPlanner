package com.khuslee.student_planner_api.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {
    @NotBlank
    @Size(max = 100)
    private String title;
    public String getTitle() { return title; }
}

