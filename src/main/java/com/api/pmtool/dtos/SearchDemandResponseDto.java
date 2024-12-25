package com.api.pmtool.dtos;

import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import java.util.UUID;

import java.time.LocalDate;

public interface SearchDemandResponseDto {
    UUID getId();

    String getProjectName();

    String getDemandName();

    Status getStatus();

    LocalDate getDueDate();

    int getDueDateChangeCount();

    LocalDate getAssignDate();

    LocalDate getStatusChangeDate();

    Priority getPriority();
    
    String getAssignedTo();
}
