package com.api.pmtool.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.enums.Status;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class ChangeDemandStatusRequestDto {

    @NotNull(message = "Demand ID is required")
    private UUID demandId;

    @NotNull(message = "New status is required")
    @Enumerated(EnumType.STRING)
    
    private Status newStatus;
    private String comment; // Optional comment

   private List<MultipartFile> multipartFiles;
}

