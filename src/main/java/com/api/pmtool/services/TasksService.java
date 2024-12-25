package com.api.pmtool.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.api.pmtool.dtos.AddCommentOnTaskDto;
import com.api.pmtool.dtos.AssignTaskRequestDto;
import com.api.pmtool.dtos.CreateTaskRequestDto;
import com.api.pmtool.dtos.SearchTasksResponseDto;
import com.api.pmtool.dtos.UpdateTaskDueDateRequestDto;
import com.api.pmtool.dtos.UpdateTaskPriorityRequestDto;
import com.api.pmtool.dtos.UpdateTaskProgressRequestDto;
import com.api.pmtool.entity.TasksEntity;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;
import com.api.pmtool.exception.ResourceNotFoundException;

public interface TasksService {

    public void createTask(CreateTaskRequestDto dto) throws IOException, DataIntegrityViolationException;

    public void updatePriority(UpdateTaskPriorityRequestDto dto) throws IOException, DataIntegrityViolationException;

    public void updateWorkProgress(UpdateTaskProgressRequestDto dto)
            throws IOException, DataIntegrityViolationException;

    public void updateDueDate(UpdateTaskDueDateRequestDto dto) throws IOException, DataIntegrityViolationException;

    public void addCommentOnTask(AddCommentOnTaskDto dto) throws IOException;

    public void assignTaskToDeveloper(AssignTaskRequestDto dto) throws IOException, DataIntegrityViolationException;

    public TasksEntity getTaskById(UUID taskId) throws IOException, DataIntegrityViolationException;

    public List<TasksEntity> getAllTasks() throws IOException, DataIntegrityViolationException;

    public Page<SearchTasksResponseDto> findTasksByCriteria(String pfId, Status status, String taskName,
            TaskType taskType,
            LocalDate startDate, LocalDate endDate, Boolean dueExceeded, UUID demandId, String demandName, UUID taskId,
            Pageable pageable) throws IOException;

    ByteArrayOutputStream getFilesAsZip(UUID commentTypeId, UUID demandId)
            throws IOException, ResourceNotFoundException, MalformedURLException;

}
