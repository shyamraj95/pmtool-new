package com.api.pmtool.dtos;

import java.util.Date;
import java.util.UUID;

import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SearchTasksResponseDto {
    private UUID id;
    private String taskName;
    private TaskType taskType;
    private Status workProgress;
    private Date dueDate;
    private Date newDueDate;
    private Priority priority;
    private int dueDateChangeCount;
    private Date completeDate;
    private String demandName;
    private String assignedToPfId;
    private String assignedToName;
    private UUID demandId;
}

