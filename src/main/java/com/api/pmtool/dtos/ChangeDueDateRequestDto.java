package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class ChangeDueDateRequestDto {
    @NotNull(message = "Demand ID is required")
    private UUID demandId;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "new due date is required")
   // @Temporal(TemporalType.DATE)
    private LocalDate newDueDate;

    private String comment; // Optional comment

   private List<MultipartFile> multipartFiles;
}
