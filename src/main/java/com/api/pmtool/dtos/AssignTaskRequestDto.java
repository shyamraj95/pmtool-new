package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.Priority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignTaskRequestDto {
    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Developer/Server Admin ID is required")
    private UUID assignToId;

    @NotNull(message = "Demand ID is required")
    private Priority priority;
    private UUID commentTypeId;

    private String comment; // Optional comment

    private List<MultipartFile> multipartFiles; // Optional file uploads
}
