package com.api.pmtool.dtos;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDemandRequestDto {
    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Department name is required")
    private String departmentName;
}
