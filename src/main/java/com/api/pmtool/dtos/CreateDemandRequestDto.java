package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.DemandTypes;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDemandRequestDto {

    @NotNull(message = "Project id is required")
    private UUID projectId;

    @NotBlank(message = "Demand name is required")
    @Size(max = 50, message = "Demand name must not exceed 50 characters")
    private String demandName;

    @NotNull(message = "Demand type is required")
    private DemandTypes demandTypes;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "comments is required")
    private String comments; 

    @NotNull(message = "comment type id is required")
    private UUID commentTypeId;

    private List<MultipartFile> files;
}
