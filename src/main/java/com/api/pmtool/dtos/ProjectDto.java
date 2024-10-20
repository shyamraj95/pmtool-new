package com.api.pmtool.dtos;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDto {
    @NotBlank(message = "Project name is required")
    private String projectName;
    @NotBlank(message = "Description is required")
    private String description;
}
