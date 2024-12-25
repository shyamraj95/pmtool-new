package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.Priority;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskPriorityRequestDto {
    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private Priority priority; 
    
    private UUID commentTypeId;

    private String comment;

    private List<MultipartFile> multipartFiles;
}
