package com.api.pmtool.controllers;

import org.springframework.web.bind.annotation.RestController;

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
import com.api.pmtool.services.DemandService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/demands")
public class DemandController {

    @Autowired
    private DemandService demandService;

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
    @GetMapping("/counts")
    public ResponseEntity<DemandCountResponseDTO> getDemandCounts() {
        DemandCountResponseDTO demandCounts = demandService.getDemandCounts();
        return ResponseEntity.ok(demandCounts);
    }

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

    @PutMapping(path = "/change-priority", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changePriority(@ModelAttribute("demand") @Valid ChangePriorityRequestDto dto) {
        try {
            demandService.changePriority(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Priority updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-demand-status", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeDemandStatus(
            @ModelAttribute("demand") @Valid ChangeDemandStatusRequestDto dto) {
        try {
            demandService.changeDemandStatus(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Status has updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "add-comment-on-demand", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> addCommentOnDemand(
            @ModelAttribute("demand") @Valid AddCommentOnDemandDto dto) {
        try {
            demandService.addCommentOnDemand(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Comment added on Demand successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(path = "/download/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadUploadedFile(@PathVariable UUID fileId) throws IOException {
        try {
            Resource resource = demandService.downloadUploadedFiles(fileId);
            // Create HTTP headers with a Content-Disposition for the downloaded file
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + fileId + "\"");
            return ResponseEntity.status(HttpStatus.OK).body(resource);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/download-files")
    public ResponseEntity<?> downloadFiles(@RequestParam UUID commentTypeId, @RequestParam UUID demandId)
            throws ResourceNotFoundException, MalformedURLException, IOException {
        ByteArrayOutputStream zipOutputStream = demandService.getFilesAsZip(commentTypeId, demandId);
        if (zipOutputStream == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayInputStream zipInputStream = new ByteArrayInputStream(zipOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(zipInputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + demandId + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }

    @GetMapping("/getAllDemands")
    @JsonIgnoreProperties({ "comments", "comments.uploads", "role" })
    public ResponseEntity<List<Demand>> getAllDemands() {
        List<Demand> demands = demandService.getAllDemands();
        return ResponseEntity.ok(demands);
    }

    @GetMapping("/{demandId}/users")
    public ResponseEntity<List<User>> getUsersByDemandId(@PathVariable UUID demandId) {
        List<User> users = demandService.getUsersByDemandId(demandId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @JsonIgnoreProperties({ "comments", "comments.uploads", "role" })
    public ResponseEntity<Page<SearchDemandResponseDto>> findDemands(
            @RequestParam(value = "pfId", required = false) String pfId,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "userRole", required = false) String userRole,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam(value = "projectName", required = false) String projectName,
            @PageableDefault(size = 10, sort = "projectName") Pageable pageable) {

        // Call service method to get the paginated and sorted demands
        Page<SearchDemandResponseDto> demands = demandService.findDemandsByCriteria(pfId, status, userRole, projectName,
                startDate, endDate, pageable);

        return ResponseEntity.ok(demands);
    }

}
