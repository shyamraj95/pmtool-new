package com.api.pmtool.services;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDemandStatusRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.ChangePriorityRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.dtos.DemandCountResponseDTO;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.Status;
import com.api.pmtool.exception.ResourceNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DemandService {
Demand createDemand(CreateDemandRequestDto demandRequestDto) throws IOException;

Demand assignDemand(AssignDemandRequestDto dto) throws IOException, IllegalArgumentException;

void changeDueDate(ChangeDueDateRequestDto dto) throws IOException;

void changePriority(ChangePriorityRequestDto dto) throws IOException;

void changeDemandStatus(ChangeDemandStatusRequestDto dto) throws IOException;

void addCommentOnDemand(AddCommentOnDemandDto dto) throws IOException;

Page<SearchDemandResponseDto> findDemandsByCriteria(String pfId, Status status, String role, String projectName, 
    LocalDate startDate, LocalDate endDate, Pageable pageable);

Demand getDemandByDemandId(UUID demandId) throws IllegalArgumentException;
List<Demand> getAllDemands();
List<User> getUsersByDemandId(UUID demandId);
DemandCountResponseDTO getDemandCounts();

Resource downloadUploadedFiles(UUID demandId)throws ResourceNotFoundException, MalformedURLException;
}
