package com.api.pmtool.services;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDemandStatusRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.dtos.DemandCountResponseDTO;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.enums.Status;

import java.io.IOException;
import java.util.UUID;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DemandService {
Demand createDemand(CreateDemandRequestDto demandRequestDto) throws IOException;

Demand assignDemand(AssignDemandRequestDto dto) throws IOException, IllegalArgumentException;

void changeDueDate(ChangeDueDateRequestDto dto) throws IOException;

void changeDemandStatus(ChangeDemandStatusRequestDto dto) throws IOException;

void addCommentOnDemand(AddCommentOnDemandDto dto) throws IOException;

Page<SearchDemandResponseDto> findDemandsByCriteria(UUID userId, Status status, String role, String projectName,Pageable pageable);

Demand getDemandByDemandId(UUID demandId) throws IllegalArgumentException;

DemandCountResponseDTO getDemandCounts();
}
