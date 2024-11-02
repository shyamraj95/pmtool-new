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
public class ChangePriorityRequestDto {
    @NotNull(message = "Demand id is required")
    private UUID demandId; 
    @NotNull(message = "Priority is required")
    private Priority newPriority;

    private UUID commentTypeId;

    private String comment; // Optional comment
    
   private List<MultipartFile> multipartFiles;
}
