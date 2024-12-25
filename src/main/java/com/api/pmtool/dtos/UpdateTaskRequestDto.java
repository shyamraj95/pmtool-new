package com.api.pmtool.dtos;

import java.time.LocalDate;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequestDto {
    @NotNull(message = "Task ID is required")
    private UUID taskId;
    private String taskName;
    private TaskType taskType;
    private Status workProgress;
    private LocalDate dueDate;
    private Priority priority; 
}
