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

/**
 * Creates a new task based on the provided request data.
 *
 * This method fetches the associated demand using the provided demand ID
 * and creates a new task entity. It sets the task's name, type, due date,
 * and initial status as 'NOT_STARTED'. A system comment is added for logging,
 * and if user comments are provided, they are added to the task along with
 * any uploaded files.
 *
 * @param taskRequestDto the DTO containing the task creation data, including
 *                       demand ID, task name, type, due date, comments, comment type ID,
 *                       and any associated files.
 * @throws IOException if an error occurs while handling file uploads.
 * @throws IllegalArgumentException if the demand or comment type cannot be found
 *                                  using the provided IDs.
 */

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
                comment.setUploads(fileUploadUtils.uploadsSetter(files, comment, demand.getId()));
                fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType, demand.getId()),commentType.getCanUploadMultipleFiles());
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

    /**
     * Updates the work progress status of a task, adds a comment to the task with the provided comment type ID and comment text,
     * and adds the provided files to the task as well.
     *
     * @param dto the DTO containing the task ID, comment type ID, comment text, files to be uploaded, and new work progress status.
     * @throws IOException if an error occurs while handling file uploads.
     * @throws IllegalArgumentException if the task or comment type cannot be found using the provided IDs.
     */
    @Override
    @Transactional
    public void updateWorkProgress(UpdateTaskProgressRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, dto.getWorkProgress(), null);
    }

    /**
     * Updates the priority of a task, adds a comment to the task with the provided comment type ID and comment text,
     * and adds the provided files to the task as well.
     *
     * @param dto the DTO containing the task ID, comment type ID, comment text, files to be uploaded, and new priority.
     * @throws IOException if an error occurs while handling file uploads.
     * @throws IllegalArgumentException if the task or comment type cannot be found using the provided IDs.
     */
    @Override
    @Transactional
    public void updatePriority(UpdateTaskPriorityRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, null, dto.getPriority());
    }

/**
 * Updates the due date of a task and adds a comment with the given details. This
 * method ensures the operation is executed within a single transaction.
 *
 * @param dto The data transfer object containing the task ID, new due date,
 *            comment type ID, comment, and files to be uploaded.
 * @throws IOException If there is an error during file handling.
 * @throws IllegalArgumentException If the task or comment type cannot be found using the provided IDs.
 */

    @Override
    @Transactional
    public void updateDueDate(UpdateTaskDueDateRequestDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), dto.getNewDueDate(), null, null);
    }

    /**
     * Adds a comment to a task, optionally with file uploads.
     *
     * @param dto the DTO containing the task ID, comment type ID, comment text, and files to be uploaded.
     * @throws IOException if an error occurs while handling file uploads.
     * @throws IllegalArgumentException if the task or comment type cannot be found using the provided IDs.
     */
    @Override
    @Transactional
    public void addCommentOnTask(AddCommentOnTaskDto dto) throws IOException {
        updateTaskDetailsAndAddComment(dto.getTaskId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, null,
                null);
    }

/**
 * Retrieves a task by its ID, including its comments and their respective uploads.
 * This method ensures that the collections are initialized to handle lazy loading.
 *
 * @param taskId The UUID of the task to be retrieved.
 * @return The TasksEntity with its comments and uploads.
 * @throws IllegalArgumentException If the task does not exist with the given ID.
 */

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

    /**
     * Retrieves all tasks from the database, including their comments and uploads.
     * The collections are initialized to handle lazy loading.
     *
     * @return A list of TasksEntity with their comments and uploads.
     */
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

/**
 * Retrieves a paginated list of tasks based on the specified search criteria.
 * The method filters tasks by optional parameters such as project ID, status, 
 * task name, task type, date range, and demand details.
 * 
 * @param pfId The ID of the project filter. Can be null.
 * @param status The status of the tasks. Can be null.
 * @param taskName The name of the task to search for. Can be null.
 * @param taskType The type of the task. Can be null.
 * @param startDate The start date for the task due date range. Can be null.
 * @param endDate The end date for the task due date range. Can be null.
 * @param dueExceeded Boolean indicating if due date has been exceeded. Can be null.
 * @param demandId The ID of the demand associated with the task. Can be null.
 * @param demandName The name of the demand associated with the task. Can be null.
 * @param taskId The unique ID of a specific task. Can be null.
 * @param pageable The pagination and sorting information.
 * @return A page of SearchTasksResponseDto containing the filtered tasks.
 * @throws IOException If an input or output exception occurs.
 */

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

/**
 * Retrieves files associated with the given comment type and task IDs, and returns 
 * them as a ZIP archive. If there are no files found, the method returns null.
 *
 * @param commentTypeId The UUID of the comment type used to filter files.
 * @param taskId The UUID of the task used to filter files.
 * @return A ByteArrayOutputStream containing the ZIP archive of files, or null if no files exist.
 * @throws IOException If an error occurs during file retrieval or ZIP creation.
 */

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


        /**
         * Updates the details of a Task and adds a comment to it, optionally with file uploads.
         * 
         * @param taskId       The UUID of the Task to update.
         * @param commentTypeId The UUID of the CommentType to associate the comment with.
         * @param comment       The comment to add to the Task.
         * @param files         The files to upload, if any.
         * @param newDueDate    The new due date to set, if any.
         * @param newStatus     The new status to set, if any.
         * @param newPriority   The new priority to set, if any.
         * @throws IOException If an error occurs while saving the Task.
         */
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
                newComment.setUploads(fileUploadUtils.uploadsSetter(files, newComment, task.getDemand().getId()));
                fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType, task.getDemand().getId()),commentType.getCanUploadMultipleFiles());
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
