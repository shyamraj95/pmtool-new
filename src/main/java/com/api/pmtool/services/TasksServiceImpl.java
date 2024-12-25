package com.api.pmtool.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.Constants.LOG_MSGS;
import com.api.pmtool.dtos.AddCommentOnTaskDto;
import com.api.pmtool.dtos.AssignTaskRequestDto;
import com.api.pmtool.dtos.CreateTaskRequestDto;
import com.api.pmtool.dtos.SearchTasksResponseDto;
import com.api.pmtool.dtos.UpdateTaskDueDateRequestDto;
import com.api.pmtool.dtos.UpdateTaskPriorityRequestDto;
import com.api.pmtool.dtos.UpdateTaskProgressRequestDto;
import com.api.pmtool.entity.CommentTypeEntity;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.entity.TasksEntity;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;
import com.api.pmtool.repository.CommentRepository;
import com.api.pmtool.repository.CommentTypeRepository;
import com.api.pmtool.repository.DemandRepository;
import com.api.pmtool.repository.TasksRepository;
import com.api.pmtool.repository.UserRepository;
import com.api.pmtool.utils.FileUploadUtils;

@Service
public class TasksServiceImpl implements TasksService {

    @Autowired
    private TasksRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private FileUploadUtils fileUploadUtils;

    @Autowired
    private CommentTypeRepository commentTypeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private final UUID SYS_JOURNEY_LOG_ID = UUID.fromString("28a0489e-9b85-4205-98e9-92ba7c5a5571");

    private static final Logger logger = LoggerFactory.getLogger(TasksServiceImpl.class);

    @Override
    @Transactional
    public void createTask(CreateTaskRequestDto taskRequestDto) throws IOException {
        // Fetch associated demand
        Demand demand = demandRepository.findById(taskRequestDto.getDemandId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demand not found with ID: " + taskRequestDto.getDemandId()));

        // Create new task
        TasksEntity task = new TasksEntity();
        task.setDemand(demand);
        task.setTaskName(taskRequestDto.getTaskName());
        task.setTaskType(taskRequestDto.getTaskType());
        task.setDueDate(taskRequestDto.getDueDate());
        task.setWorkProgress(Status.NOT_STARTED);

        // Add a system comment for logging
        String message = LOG_MSGS.TASK_CREATED + " with name " + taskRequestDto.getTaskName();
        task.getComments().add(systemComment(task, message));

        // Add user comment if provided
        if (taskRequestDto.getComments() != null) {
            CommentTypeEntity commentType = commentTypeRepository.findById(taskRequestDto.getCommentTypeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Comment Type not found with ID: " + taskRequestDto.getCommentTypeId()));
            Comments comment = new Comments();
            comment.setCommentType(commentType);
            comment.setComment(taskRequestDto.getComments());
            comment.setTask(task);
            comment.setDemand(demand);
            List<MultipartFile> files = taskRequestDto.getFiles();
            if (files != null && !files.isEmpty()) {
                comment.setUploads(fileUploadUtils.uploadsSetter(files, comment));
                fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType));
            }

            task.getComments().add(comment);
        }

        taskRepository.save(task);

    }

    @Override
    @Transactional
    public void assignTaskToDeveloper(AssignTaskRequestDto dto) throws IOException {
        // Fetch the task by ID
        TasksEntity task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + dto.getTaskId()));

        // Assign developer
        User developer = userRepository.findById(dto.getAssignToId())
                .orElseThrow(
                        () -> new IllegalArgumentException("User not found with ID: " + dto.getAssignToId()));

        task.setAssignedTo(developer);

        // Set priority and due date
        task.setPriority(dto.getPriority());

        // Add system comment for logging
        String sysMessage = LOG_MSGS.TASK_ASSIGNED + " " + developer.getFullName();
        task.getComments().add(systemComment(task, sysMessage));

        // Save task
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void updateWorkProgress(UpdateTaskProgressRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, dto.getWorkProgress(), null);
    }

    @Override
    @Transactional
    public void updatePriority(UpdateTaskPriorityRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, null, dto.getPriority());
    }

    @Override
    @Transactional
    public void updateDueDate(UpdateTaskDueDateRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), dto.getNewDueDate(), null, null);
    }

    @Override
    @Transactional
    public void addCommentOnTask(AddCommentOnTaskDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, null,
                null);
    }

    @Override
    @Transactional
    public TasksEntity getTaskById(UUID taskId) {
        // Fetch task with comments and uploads
        TasksEntity task = taskRepository.findByIdWithCommentsAndUploads(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
        // Initialize collections
        task.getDemand();
        task.getComments().forEach(comment -> comment.getUploads().size());
        return task;
    }

    @Override
    @Transactional
    public List<TasksEntity> getAllTasks() {
        List<TasksEntity> tasks = taskRepository.findAll();
        tasks.forEach(task -> {
            task.getDemand();
            task.getComments().forEach(comment -> comment.getUploads().size());
        });
        logger.debug("Tasks fetched: {}", tasks);
        return tasks;
    }

    @Override
    public Page<SearchTasksResponseDto> findTasksByCriteria(String pfId, Status status, String taskName,
            TaskType taskType,
            LocalDate startDate, LocalDate endDate, Boolean dueExceeded, UUID demandId, String demandName, UUID taskId,
            Pageable pageable) throws IOException {
        Page<TasksEntity> tasks = taskRepository.findTasksByCriteria(pfId, status, taskName, taskType, startDate,
                endDate, dueExceeded, demandId, demandName, taskId, pageable);
        return tasks.map(this::mapToDto);
    }

    private SearchTasksResponseDto mapToDto(TasksEntity task) {
        SearchTasksResponseDto dto = new SearchTasksResponseDto();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setTaskType(task.getTaskType());
        dto.setWorkProgress(task.getWorkProgress());
        dto.setDueDate(task.getDueDate());
        dto.setNewDueDate(task.getNewDueDate());
        dto.setPriority(task.getPriority());
        dto.setCompleteDate(task.getCompleteDate());
        dto.setDueDateChangeCount(task.getDueDateChangeCount());
        if (task.getAssignedTo() != null) {
            dto.setAssignedToPfId(task.getAssignedTo().getPfId());
            dto.setAssignedToName(task.getAssignedTo().getFullName());
        }
        if (task.getDemand() != null) {
            dto.setDemandName(task.getDemand().getDemandName());
            dto.setDemandId(task.getDemand().getId());
        }

        return dto;
    }

    @Transactional
    @Override
    public ByteArrayOutputStream getFilesAsZip(UUID commentTypeId, UUID taskId) throws IOException {
        List<Path> filePaths = commentRepository.findByTypeIdAndTaskId(commentTypeId, taskId)
                .stream()
                .flatMap(comment -> comment.getUploads().stream())
                .map(upload -> Paths.get(upload.getFilePath()))
                .collect(Collectors.toList());

        // Check if there are files to download
        if (filePaths.isEmpty()) {
            return null; // Or throw an exception if you prefer
        }
        return fileUploadUtils.getFilesAsZip(filePaths);
    }


    private void updateTaskDetailsAndAddComment(UUID taskId, UUID commentTypeId, String comment,
            List<MultipartFile> files, Date newDueDate, Status newStatus, Priority newPriority)
            throws IOException {
        TasksEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (newDueDate != null) {
            task.getComments().add(systemComment(task, LOG_MSGS.TASK_DUE_DATE_CHANGE));
            task.extendDueDate(newDueDate);
        }

        if (newPriority != null) {
            task.getComments().add(systemComment(task, LOG_MSGS.TASK_PRIORITY_CHANGE));
            task.setPriority(newPriority);
        }

        if (newStatus != null) {
            task.getComments().add(systemComment(task, LOG_MSGS.TASK_STATUS_CHANGE));
            task.setWorkProgress(newStatus);
        }

        if (comment != null && !comment.isEmpty()) {
            CommentTypeEntity commentType = commentTypeRepository.findById(commentTypeId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Comment Type not found with ID: " + commentTypeId));
            Comments newComment = new Comments();
            newComment.setCommentType(commentType);
            newComment.setComment(comment);
            newComment.setTask(task);
            newComment.setDemand(task.getDemand());

            if (files != null && !files.isEmpty()) {
                newComment.setUploads(fileUploadUtils.uploadsSetter(files, newComment));
                fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType));
            }

            task.getComments().add(newComment);
        }

        taskRepository.save(task);
    }

    private Comments systemComment(TasksEntity task, String message) {
        Comments comment = new Comments();
        CommentTypeEntity commentType = commentTypeRepository.findById(SYS_JOURNEY_LOG_ID)
                .orElseThrow(() -> new IllegalArgumentException("System comment type ID not found"));

        Demand demand = demandRepository.findById(task.getDemand().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demand not found with ID: " + task.getDemand().getId()));
        comment.setCommentType(commentType);
        comment.setComment(message);
        comment.setDemand(demand);
        comment.setTask(task);
        return comment;
    }
}
