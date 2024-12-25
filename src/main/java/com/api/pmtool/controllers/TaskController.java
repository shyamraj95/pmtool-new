package com.api.pmtool.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.dtos.AddCommentOnTaskDto;
import com.api.pmtool.dtos.AssignTaskRequestDto;
import com.api.pmtool.dtos.CreateTaskRequestDto;
import com.api.pmtool.dtos.SearchTasksResponseDto;
import com.api.pmtool.dtos.UpdateTaskDueDateRequestDto;
import com.api.pmtool.dtos.UpdateTaskPriorityRequestDto;
import com.api.pmtool.dtos.UpdateTaskProgressRequestDto;
import com.api.pmtool.dtos.UpdateTaskRequestDto;
import com.api.pmtool.entity.TasksEntity;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;
import com.api.pmtool.exception.ResourceNotFoundException;
import com.api.pmtool.services.TasksService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TasksService taskService;

    @PostMapping(path = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> createTask(
            @ModelAttribute("task") @Valid CreateTaskRequestDto taskDTO) {
        try {
            taskService.createTask(taskDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping(path = "/assign", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> assignTask(
            @ModelAttribute("task") @Valid AssignTaskRequestDto assignTaskRequestDto) {
        try {
            taskService.assignTaskToDeveloper(assignTaskRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("SUCCESS");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-due-date", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeDueDate(
            @ModelAttribute("task") @Valid UpdateTaskDueDateRequestDto dto) {
        try {
            taskService.updateDueDate(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Due date updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-priority", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changePriority(@ModelAttribute("task") @Valid UpdateTaskPriorityRequestDto dto) {
        try {
            taskService.updatePriority(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Priority updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/change-status", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> changeTaskStatus(
            @ModelAttribute("task") @Valid UpdateTaskProgressRequestDto dto) {
        try {
            taskService.updateWorkProgress(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Status updated successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(path = "/add-comment", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> addCommentOnTask(
            @ModelAttribute("task") @Valid AddCommentOnTaskDto dto) {
        try {
            taskService.addCommentOnTask(dto);
            return ResponseEntity.status(HttpStatus.OK).body("Comment added on Task successfully.");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /*
     * @PostMapping("/download-files")
     * public ResponseEntity<?> downloadFiles(@RequestParam UUID
     * commentTypeId, @RequestParam UUID taskId)
     * throws ResourceNotFoundException, MalformedURLException, IOException {
     * ByteArrayOutputStream zipOutputStream =
     * taskService.getFilesAsZip(commentTypeId, taskId);
     * if (zipOutputStream == null) {
     * return ResponseEntity.notFound().build();
     * }
     * 
     * ByteArrayInputStream zipInputStream = new
     * ByteArrayInputStream(zipOutputStream.toByteArray());
     * InputStreamResource resource = new InputStreamResource(zipInputStream);
     * 
     * HttpHeaders headers = new HttpHeaders();
     * headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + taskId
     * + ".zip");
     * 
     * return ResponseEntity.ok()
     * .headers(headers)
     * .contentType(MediaType.APPLICATION_OCTET_STREAM)
     * .body(resource);
     * }
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable UUID taskId)
            throws DataIntegrityViolationException, IOException {
        try {
            return ResponseEntity.ok(taskService.getTaskById(taskId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/getAllTasks")
    @JsonIgnoreProperties({ "comments", "comments.uploads" })
    public ResponseEntity<List<TasksEntity>> getAllTasks() throws DataIntegrityViolationException, IOException {
        List<TasksEntity> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/search")
    @JsonIgnoreProperties({ "comments", "comments.uploads" })
    public ResponseEntity<Page<SearchTasksResponseDto>> findTasks(
            @RequestParam(value = "pfId", required = false) String pfId,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam(value = "dueExceeded", required = false) Boolean dueExceeded,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "taskType", required = false) TaskType taskType,
            @RequestParam(value = "demandId", required = false) UUID demandId,
            @RequestParam(value = "demandName", required = false) String demandName,
            @RequestParam(value = "taskId", required = false) UUID taskId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) throws IOException {

        // Call service method to get the paginated and sorted tasks
        Page<SearchTasksResponseDto> tasks = taskService.findTasksByCriteria(pfId, status, taskName, taskType,
                startDate, endDate, dueExceeded, demandId, demandName, taskId, pageable);

        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/download-files")
    public ResponseEntity<?> downloadFiles(@RequestParam UUID commentTypeId, @RequestParam UUID taskId)
            throws ResourceNotFoundException, MalformedURLException, IOException {
        ByteArrayOutputStream zipOutputStream = taskService.getFilesAsZip(commentTypeId, taskId);
        if (zipOutputStream == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayInputStream zipInputStream = new ByteArrayInputStream(zipOutputStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(zipInputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + taskId + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }
}