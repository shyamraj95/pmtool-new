package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskProgressRequestDto {
    @NotNull(message = "Task ID is required")
    private UUID taskId;
    
    @NotNull(message = "New status is required")
    @Enumerated(EnumType.STRING)
    private Status workProgress;

    private UUID commentTypeId;

    private String comment;

    private List<MultipartFile> multipartFiles;
}
