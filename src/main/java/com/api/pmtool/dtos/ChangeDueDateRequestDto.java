package com.api.pmtool.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class ChangeDueDateRequestDto {
    @NotNull(message = "Demand ID is required")
    private UUID demandId;

    @NotNull(message = "new due date is required")
    @Temporal(TemporalType.DATE)
    private Date newDueDate;

    private String comment; // Optional comment

   private List<MultipartFile> multipartFiles;
}
