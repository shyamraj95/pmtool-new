package com.api.pmtool.dtos;

import com.api.pmtool.enums.Status;

import java.util.UUID;
import java.util.Date;


    public interface SearchDemandResponseDto {
        UUID getId();
        String getProjectName();
        String getDemandName();
        Status getStatus();  // Assuming you have an enum 'Status' for demand status
        Date getDueDate();
        int getDueDateChangeCount();
    
    }
