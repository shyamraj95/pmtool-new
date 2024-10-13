package com.api.pmtool.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.repository.UserRepository;
import com.api.pmtool.services.DemandService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

/*     @PutMapping("/{id}/update")
    public ResponseEntity<Demand> updateDemand(
            @PathVariable UUID id,
            @ModelAttribute("demand") @Valid CreateDemandRequestDto demandDTO) {
                try {
                    Demand createdDemand = demandService.updateDemand(demandDTO);
                    return ResponseEntity.status(HttpStatus.CREATED).body(createdDemand);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
    } */
    
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
    @PutMapping(path ="change-due-date", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeDueDate(
        @ModelAttribute("demand") @Valid ChangeDueDateRequestDto dto) { 
            try {
                demandService.changeDueDate(dto);
                return ResponseEntity.status(HttpStatus.OK).body("Due date updated successfully.");
            } catch (IllegalArgumentException | IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
    }
    @PutMapping(path ="add-comment-on-demand", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
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
    public ResponseEntity<Page<Demand>> findDemands(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "userRole", required = false) String userRole,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "dueDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        // Call service method to get the paginated and sorted demands
        Page<Demand> demands = demandService.findDemandsByCriteria(userId, userRole, projectName, page, size, sortBy, sortDir);

        return ResponseEntity.ok(demands);
    }
/*    

    // API to update demand project and department
    @PutMapping("/{id}/update")
    public ResponseEntity<Demand> updateDemand(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDemandRequestDto dto) {
        Demand updatedDemand = demandService.updateDemand(id, dto);
        return ResponseEntity.ok(updatedDemand);
    }
        // API to assign demand to users (assignee, tech lead, and developers)
        @PostMapping("/assign")
        public ResponseEntity<Demand> assignDemandToUsers(
                @Valid @RequestBody AssignDemandRequestDto dto) {
            Demand updatedDemand = demandService.assignDemandToUsers(dto);
            return ResponseEntity.ok(updatedDemand);
        }
        // API to create a new demand
            @Parameter(
            description = "Files to be uploaded", 
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
    // API to get demand by ID
    @GetMapping("/{id}")
    public ResponseEntity<Demand> getDemandById(
            @PathVariable UUID id) {
        Demand demand = demandService.getDemandById(id);
        return ResponseEntity.ok(demand);
    }
    @PutMapping("/{id}/due-date")
    public ResponseEntity<Demand> changeDueDate(
            @PathVariable UUID id, 
            @Valid @RequestBody ChangeDueDateRequestDto dto,
            @RequestHeader("userId") UUID userId) {  // User ID passed via request header
        Demand updatedDemand = demandService.changeDueDate(id, dto.getNewDueDate(), dto.getComments(), userId);
        return ResponseEntity.ok(updatedDemand);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Demand> changeDemandStatus(
            @PathVariable UUID id, 
            @Valid @RequestBody ChangeDemandStatusRequestDto dto) {
        Demand updatedDemand = demandService.changeDemandStatus(id, dto.getNewStatus());
        return ResponseEntity.ok(updatedDemand);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Demand>> searchDemands(
            @RequestParam(value = "assignee", required = false) UUID assigneeId,
            @RequestParam(value = "techLead", required = false) UUID techLeadId,
            @RequestParam(value = "department", required = false) String department,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Demand> demands = demandService.findDemandsByUserOrDepartment(assigneeId, techLeadId, department, pageable);
        return ResponseEntity.ok(demands);
    } */
}
