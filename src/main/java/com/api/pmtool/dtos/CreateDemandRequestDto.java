package com.api.pmtool.dtos;


import java.util.List;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CreateDemandRequestDto {

    @NotBlank(message = "Project name is required")
    private String projectName;
    @NotBlank(message = "Demand name is required")
    @Size(max = 15, message = "Demand name must not exceed 15 characters")
    private String demandName;

    @NotBlank(message = "Demand type is required")
    private String demandType;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String comments;  // Comments and/or files to be uploaded

    private List<MultipartFile> files;
}

