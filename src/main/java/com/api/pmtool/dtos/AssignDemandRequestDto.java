package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.Priority;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class AssignDemandRequestDto {

    @NotNull(message = "Demand ID is required")
    private UUID demandId;

    @NotNull(message = "Demand ID is required")
    private Priority priority;

    @NotNull(message = "Assignee user ID is required")
    private UUID assigneeId;

    @NotNull(message = "Tech Lead user ID is required")
    private UUID techLeadId;

    @NotNull(message = "Developer IDs are required")
    private List<UUID> developerIds;
    
     private String comment; // Optional comment

    private List<MultipartFile> multipartFiles; // Optional file uploads

}

