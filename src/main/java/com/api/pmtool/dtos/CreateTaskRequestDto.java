package com.api.pmtool.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.TaskType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequestDto {
    @NotNull(message = "Demand ID is required")
    private UUID demandId;

    @NotNull(message = "Task Name is required")
    private String taskName;

    @NotNull(message = "Task Type is required")
    private TaskType taskType;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "Due date is required")
    private Date dueDate;

    @NotNull(message = "comments is required")
    private String comments;

    @NotNull(message = "comment type id is required")
    private UUID commentTypeId;

    private List<MultipartFile> files;
}
