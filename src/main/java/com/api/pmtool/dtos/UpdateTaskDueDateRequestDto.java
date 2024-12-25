package com.api.pmtool.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskDueDateRequestDto {
    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Due date is required")
    private Date newDueDate;

    private UUID commentTypeId;

    private String comment;

    private List<MultipartFile> multipartFiles;
}
