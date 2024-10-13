package com.api.pmtool.dtos;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.api.pmtool.enums.Status;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class ChangeDemandStatusRequestDto {
    @NotNull(message = "New status is required")
    @Enumerated(EnumType.STRING)
    private Status newStatus;
}

