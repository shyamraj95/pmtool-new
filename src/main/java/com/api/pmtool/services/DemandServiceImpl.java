package com.api.pmtool.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.Constants.LOG_MSGS;
import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDemandStatusRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.ChangePriorityRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.dtos.DemandCountResponseDTO;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.CommentTypeEntity;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.entity.ProjectEntity;
import com.api.pmtool.entity.Uploads;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.exception.ResourceNotFoundException;
import com.api.pmtool.repository.CommentRepository;
import com.api.pmtool.repository.CommentTypeRepository;
import com.api.pmtool.repository.DemandRepository;
import com.api.pmtool.repository.ProjectRepository;
import com.api.pmtool.repository.UploadRepository;
import com.api.pmtool.repository.UserRepository;
import com.api.pmtool.utils.FileUploadUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
public class DemandServiceImpl implements DemandService {

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FileUploadUtils fileUploadUtils;

    @Autowired
    private CommentTypeRepository commentTypeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UploadRepository uploadRepository;


    private final UUID SYS_JOURNEY_LOG_ID = UUID.fromString("28a0489e-9b85-4205-98e9-92ba7c5a5571");

    private static final Logger logger = LoggerFactory.getLogger(DemandService.class);

/**
 * Creates a new demand based on the provided demand request DTO.
 * This method performs the following actions:
 * 1. Fetches the associated project using the project ID from the request.
 * 2. Creates a new Demand entity and sets its properties, including
 *    project association, demand name, demand type, due date, and initial status.
 * 3. Fetches the comment type and creates a system comment for logging the demand creation.
 * 4. Creates a user comment and associates it with the demand.
 * 5. Handles file uploads associated with the comment, if any, and saves them.
 * 6. Persists the Demand entity along with its comments and uploads.
 *
 * @param demandRequestDto the data transfer object containing information
 *                         required to create a new demand.
 * @return the created and saved Demand entity.
 * @throws IOException if an error occurs during file handling.
 * @throws IllegalArgumentException if the project or comment type cannot be found with the given IDs.
 */

    @Override
    @Transactional // Ensures that the entire method executes within a single transaction
    public Demand createDemand(CreateDemandRequestDto demandRequestDto) throws IOException {
        // Fetch the associated project
        ProjectEntity project = projectRepository.findById(demandRequestDto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found with ID: " + demandRequestDto.getProjectId()));
        // Create a new Demand entity
        Demand demand = new Demand();
        demand.setProject(project); // Associate project with demand
        demand.setDemandName(demandRequestDto.getDemandName());
        demand.setDemandTypes(demandRequestDto.getDemandTypes());
        demand.setDueDate(demandRequestDto.getDueDate());
        demand.setStatus(Status.NOT_STARTED);
        CommentTypeEntity commentType = commentTypeRepository.findById(demandRequestDto.getCommentTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comment Type not found with ID: " + demandRequestDto.getCommentTypeId()));

        // add System Comment for Logging
        String message= LOG_MSGS.DEMAND_CREATED + " with name " +demandRequestDto.getDemandName();
        demand.getComments().add(systemComment(demand, message));

        // Create and set the Comment
        Comments comment = new Comments();
        comment.setCommentType(commentType);
        comment.setComment(demandRequestDto.getComments());
        comment.setDemand(demand); // Set bidirectional relationship with Demand

        demand.getComments().add(comment);
        List<MultipartFile> files = demandRequestDto.getFiles();
        if (files != null && files.size() > 0) {
            comment.setUploads(fileUploadUtils.uploadsSetter(files, comment, demand.getId())); // Set bidirectional relationship for Uploads
        }
        Demand savedData = demandRepository.save(demand);
        fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, comment.getCommentType(), demand.getId()),commentType.getCanUploadMultipleFiles());
        return savedData;
    }

/**
 * Assigns a demand to a manager and tech lead, sets its priority and assignment date,
 * and adds comments if provided. This method ensures that the operation is executed
 * within a single transaction.
 *
 * @param dto The data transfer object containing the demand assignment details.
 * @throws IOException If there is an error during file handling.
 * @throws IllegalArgumentException If any of the provided IDs do not correspond to
 *                                  existing entities in the database.
 */

    @Override
    @Transactional // Ensure the operation happens in a single transaction
    public void assignDemand(AssignDemandRequestDto dto) throws IOException, IllegalArgumentException {
        // Fetch the demand by demandId
        Demand demand = demandRepository.findById(dto.getDemandId())
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + dto.getDemandId()));

        // Assign Manager (MANAGER role)
        User manager = userRepository.findById(dto.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + dto.getAssigneeId()));

        demand.setAssignedTo(manager);
        // Assign Tech Lead (TECH_LEAD role)
        User techLead = userRepository.findById(dto.getTechLeadId())
                .orElseThrow(() -> new IllegalArgumentException("Tech Lead not found with ID: " + dto.getTechLeadId()));
        demand.setTechLead(techLead);
        demand.setPriority(dto.getPriority()); // Set priority
        demand.setAssignDate(LocalDate.now());

        CommentTypeEntity commentType = commentTypeRepository.findById(dto.getCommentTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comment Type not found with ID: " + dto.getCommentTypeId()));
        // add System Comment for Logging
        String systemMessage = LOG_MSGS.DEMAND_ASSIGNED + " " + manager.getFullName() + " and "
                + techLead.getFullName();
        demand.getComments().add(systemComment(demand, systemMessage));
        List<MultipartFile> files = dto.getMultipartFiles();
        // Associate the Comment with Demand
        if (dto.getComment() != null && !dto.getComment().isEmpty()) {
            Comments userComment = new Comments();
            userComment.setCommentType(commentType);
            userComment.setComment(dto.getComment());
            userComment.setDemand(demand); // Set the bidirectional relationship

            if (files != null && files.size() > 0) {
                userComment.setUploads(fileUploadUtils.uploadsSetter(files, userComment, demand.getId())); // Set bidirectional relationship for Uploads
            }

            demand.getComments().add(userComment); // Update the comments list
        }
        // }

        // Save Demand, which will cascade and save Comments and Uploads due to
        demandRepository.save(demand);
        fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType, demand.getId()),commentType.getCanUploadMultipleFiles());
    }

    /**
     * Retrieves a Demand entity from the database by its ID, including its comments
     * and their respective uploads. This method is transactional and will throw an
     * IllegalArgumentException if the ID does not correspond to an existing Demand
     * entity in the database.
     *
     * @param demandId The ID of the Demand to be retrieved.
     * @return The Demand entity with its comments and uploads.
     * @throws IllegalArgumentException If the Demand does not exist with the given ID.
     */
    @Override
    @Transactional
    public Demand getDemandByDemandId(UUID demandId) throws IllegalArgumentException {
        // Fetch the demand by its ID, including the comments and their uploads
        Demand demand = demandRepository.findByIdWithCommentsAndUploads(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // Initialize tasks, comments and uploads (to handle lazy loading)
        demand.getProject();
        demand.getTasks().size();
        demand.getComments().forEach(comment -> comment.getUploads().size());
        return demand;

    }


    /**
     * Updates the due date of a Demand and adds a comment with the given details. This
     * method ensures that the operation is executed within a single transaction.
     * 
     * @param dto The data transfer object containing the demand ID, new due date,
     *            comment type ID, comment, and files to be uploaded.
     * @throws IOException If there is an error during file handling.
     * @throws IllegalArgumentException If any of the provided IDs do not correspond to
     *                                  existing entities in the database.
     */
    @Override
    @Transactional
    public void changeDueDate(ChangeDueDateRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(),
                dto.getNewDueDate(), null, null);
    }


    @Override
    @Transactional
    public void changeDemandStatus(ChangeDemandStatusRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null,
                dto.getNewStatus(), null);
    }

    /**
     * Updates the priority of a Demand and adds a comment with the given details. This
     * method ensures that the operation is executed within a single transaction.
     * 
     * @param dto The data transfer object containing the demand ID, new priority,
     *            comment type ID, comment, and files to be uploaded.
     * @throws IOException If there is an error during file handling.
     * @throws IllegalArgumentException If any of the provided IDs do not correspond to
     *                                  existing entities in the database.
     */
    @Transactional
    @Override
    public void changePriority(ChangePriorityRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null,
                null, dto.getNewPriority());
    }

/**
 * Downloads an uploaded file based on the provided file ID.
 *
 * @param fileId The UUID of the file to be downloaded.
 * @return A Resource representing the file to be downloaded.
 * @throws ResourceNotFoundException If the file is not found in the repository
 *                                   or the file does not exist at the specified path.
 * @throws MalformedURLException If the file path is malformed.
 */

    @Override
    public Resource downloadUploadedFiles(UUID fileId) throws ResourceNotFoundException, MalformedURLException {
        Uploads upload = uploadRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId, null, null));

        // Use the stored file path to serve the file
        Path filePath = Paths.get(upload.getFilePath()).normalize();

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found with ID: " + fileId, null, null);
        }
        return resource;
    }

    /**
     * Returns a ByteArrayOutputStream containing the files associated with the given
     * comment type and demand IDs as a ZIP archive. If there are no files to download,
     * the method will return null.
     *
     * @param commentTypeId The UUID of the comment type to associate the files with.
     * @param demandId      The UUID of the demand to associate the files with.
     * @return A ByteArrayOutputStream containing the ZIP archive, or null if there are no
     *         files to download.
     * @throws IOException If an error occurs while reading the file input streams.
     */
    @Override
    public ByteArrayOutputStream getFilesAsZip(UUID commentTypeId, UUID demandId) throws IOException {
        List<Path> filePaths = commentRepository.findByTypeIdAndDemandId(commentTypeId, demandId)
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
     * Adds a comment to a Demand, optionally with file uploads.
     * 
     * @param dto The AddCommentOnDemandDto containing the request data
     * @throws IOException
     *                     If there is an error while saving the Demand
     */
    @Override
    @Transactional
    public void addCommentOnDemand(AddCommentOnDemandDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getCommentTypeId(), dto.getComment(),
                dto.getMultipartFiles(), null, null,
                null);
    }


    /**
     * Updates the details of a Demand and adds a comment to it, optionally with file uploads.
     * 
     * @param demandId       The UUID of the Demand to update.
     * @param commentTypeId  The UUID of the CommentType to associate the comment with.
     * @param comment        The comment to add to the Demand.
     * @param files          The files to upload, if any.
     * @param newDueDate     The new due date to set, if any.
     * @param status         The new status to set, if any.
     * @param newPriority    The new priority to set, if any.
     * @throws IOException If an error occurs while saving the Demand.
     */
    private void UpdateDemandDetailsAndAddComment(UUID demandId, @Nullable UUID commentTypeId, @Nullable String comment,
            @Nullable List<MultipartFile> files,
            @Nullable LocalDate newDueDate, @Nullable Status status, @Nullable Priority newPriority)
            throws IOException {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // demand.getComments().forEach(comment -> comment.getUploads().size());
        // demand.getUserRoles().size();
        if (newPriority != null) {
            // Extend due date using the existing method and increment dueDateChangeCount
            // add System Comment for Logging
            demand.getComments().add(systemComment(demand, LOG_MSGS.DEMAND_PRIORITY_CHANGE));
            demand.setPriority(newPriority);
        }
        if (newDueDate != null) {
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.getComments().add(systemComment(demand, LOG_MSGS.DEMAND_DUE_DATE_CHANGE));
            demand.extendDueDate(newDueDate);
        }
        if (status != null) {
            demand.getComments().add(systemComment(demand, LOG_MSGS.DEMAND_STATUS_CHANGE));
            demand.setStatusChangeDate(LocalDate.now());
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.setStatus(status); // Update the status of the demand with the new status;
        }

        // Associate the Comment with Demand
        if (comment != null && !comment.isEmpty() && commentTypeId != null) {
            CommentTypeEntity commentType = commentTypeRepository.findById(commentTypeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Comment Type not found with ID: " + commentTypeId));
            Comments newComment = new Comments();
            newComment.setCommentType(commentType);
            newComment.setComment(comment);
            newComment.setDemand(demand); // Set the bidirectional relationship
            newComment.setUploads(fileUploadUtils.uploadsSetter(files, newComment, demand.getId()));
            // Add the new comment to the list of existing comments
            List<Comments> currentComments = demand.getComments();
            if (currentComments == null) {
                currentComments = new ArrayList<>();
            }
            currentComments.add(newComment);
            demand.setComments(currentComments); // Update the comments list
            if (files != null && files.size() > 0) {
                fileUploadUtils.saveFile(files, fileUploadUtils.getFilesPath(files, commentType, demand.getId()),commentType.getCanUploadMultipleFiles());
            }

        }
        // Save the demand (this will cascade and save the new comment and upload as
        // well)
        demandRepository.save(demand);
    }

/**
 * Creates a system comment for a given demand with an optional action log message.
 *
 * @param demand The demand to associate the system comment with.
 * @param actionLogMessage An optional log message describing the action to be logged.
 *                          If null, no action message will be set.
 * @return A Comments object representing the system comment associated with the demand.
 * @throws IllegalArgumentException If the system journey log comment type ID is not found.
 */

    private Comments systemComment(Demand demand, @Nullable String actionLogMessage) {
        Comments loggerComment = new Comments();
        if (actionLogMessage != null) {
            CommentTypeEntity commentType = commentTypeRepository.findById(SYS_JOURNEY_LOG_ID)
                    .orElseThrow(() -> new IllegalArgumentException("Journey comment type id not found "));
            loggerComment.setCommentType(commentType);
            loggerComment.setComment(actionLogMessage);
            loggerComment.setDemand(demand);
        }
        return loggerComment;
    }

    @Override
    @Transactional
    public List<Demand> getAllDemands() {
        List<Demand> demands = demandRepository.findAll();
        demands.forEach((Demand demand) -> {
            // demand.getUserRoles().size();
            demand.getProject();
            demand.getTasks().size();
            // Access the comments to initialize the collection
            demand.getComments().size();
            demand.getComments().forEach(comment -> comment.getUploads().size());
        });
        logger.debug("Demands fetched: {}", demands);
        return demands;
    }

    /**
     * Retrieves a page of {@link SearchDemandResponseDto} objects, based on the
     * given criteria.
     * 
     * @param userId      The ID of the user who created the demands; nullable.
     * @param status      The status of the demands; nullable.
     * @param role        The role of the user in the demands; nullable.
     * @param projectName The project name of the demands; nullable.
     * @param pageable    The pagination and sorting criteria.
     * @return A page of {@link SearchDemandResponseDto} objects, or an empty page
     *         if no demands are found.
     */

    @Override
    public Page<SearchDemandResponseDto> findDemandsByCriteria(String pfId,
            Status status, String projectName, LocalDate startDate, LocalDate endDate, Boolean dueExceeded,String demandName, UUID demandId,
            Pageable pageable) {
        // Create a Pageable object for pagination and sorting
        Page<SearchDemandResponseDto> demands = demandRepository.searchDemands(pfId,
                status, projectName, startDate, endDate, dueExceeded,demandName,demandId, pageable);
        logger.debug("Demands fetched: {}", demands.getContent());
        return demands;
    }

    /**
     * Retrieves a {@link DemandCountResponseDTO} containing the counts of demands
     * by status, as well as the count of demands due this week and the count of
     * demands whose due date has been changed.
     * 
     * @return A {@link DemandCountResponseDTO} containing the counts of demands.
     */
    @Override
    public DemandCountResponseDTO getDemandCounts() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(DayOfWeek.SUNDAY);
        return demandRepository.fetchDemandCounts(startOfWeek, endOfWeek, currentDate);

    }

}
/*
 * 
 * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 * LocalDate newDueDate = LocalDate.parse("30/08/1988", formatter);
 * 
 */
