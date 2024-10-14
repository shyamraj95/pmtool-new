package com.api.pmtool.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDemandStatusRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.repository.UserRepository;
import com.api.pmtool.services.DemandService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/demands")
public class DemandController {

    @Autowired
    private DemandService demandService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Demand> createDemand(
            @ModelAttribute("demand") @Valid CreateDemandRequestDto demandDTO) {
        try {
            Demand createdDemand = demandService.createDemand(demandDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDemand);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * @PutMapping("/{id}/update")
     * public ResponseEntity<Demand> updateDemand(
     * 
     * @PathVariable UUID id,
     * 
     * @ModelAttribute("demand") @Valid CreateDemandRequestDto demandDTO) {
     * try {
     * Demand createdDemand = demandService.updateDemand(demandDTO);
     * return ResponseEntity.status(HttpStatus.CREATED).body(createdDemand);
     * } catch (Exception e) {
     * e.printStackTrace();
     * return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     * }
     * }
     */

    @PutMapping(path = "/assign", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> assignDemand(
            @ModelAttribute("demand") @Valid AssignDemandRequestDto assignDemandRequestDto) {
        try {
            // Assign the demand and return the updated entity
            demandService.assignDemand(assignDemandRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("SUCCESS");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{demandId}")
    public ResponseEntity<?> getDemandByDemandId(@PathVariable UUID demandId) {
        try {
            // Return the list of comments (with uploads)
            return ResponseEntity.ok(demandService.getDemandByDemandId(demandId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "change-due-date", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeDueDate(
            @ModelAttribute("demand") @Valid ChangeDueDateRequestDto dto) {
        try {
            demandService.changeDueDate(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Due date updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-demand-status", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeDemandStatus(
            @ModelAttribute("demand") @Valid ChangeDemandStatusRequestDto dto) {
        try {
            demandService.changeDemandStatus(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Due date updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "add-comment-on-demand", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> addCommentOnDemand(
            @ModelAttribute("demand") @Valid AddCommentOnDemandDto dto) {
        try {
            demandService.addCommentOnDemand(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Due date updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    @JsonIgnoreProperties({ "comments", "comments.uploads", "role" })
    public ResponseEntity<Page<SearchDemandResponseDto>> findDemands(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "userRole", required = false) String userRole,
            @RequestParam(value = "projectName", required = false) String projectName,
            @PageableDefault(size = 10, sort = "projectName") Pageable pageable) {

        // Call service method to get the paginated and sorted demands
        Page<SearchDemandResponseDto> demands = demandService.findDemandsByCriteria(userId, userRole, projectName,
                pageable);

        return ResponseEntity.ok(demands);
    }

    /*
     * @PutMapping("/{id}/status")
     * public ResponseEntity<Demand> changeDemandStatus(
     * 
     * @PathVariable UUID id,
     * 
     * @Valid @RequestBody ChangeDemandStatusRequestDto dto) {
     * Demand updatedDemand = demandService.changeDemandStatus(id,
     * dto.getNewStatus());
     * return ResponseEntity.ok(updatedDemand);
     * }
     * 
     * @GetMapping("/search")
     * public ResponseEntity<Page<Demand>> searchDemands(
     * 
     * @RequestParam(value = "assignee", required = false) UUID assigneeId,
     * 
     * @RequestParam(value = "techLead", required = false) UUID techLeadId,
     * 
     * @RequestParam(value = "department", required = false) String department,
     * 
     * @PageableDefault(size = 10) Pageable pageable) {
     * Page<Demand> demands =
     * demandService.findDemandsByUserOrDepartment(assigneeId, techLeadId,
     * department, pageable);
     * return ResponseEntity.ok(demands);
     * }
     */
}
