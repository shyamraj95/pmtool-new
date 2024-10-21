package com.api.pmtool.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDemandStatusRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.ChangePriorityRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.dtos.DemandCountResponseDTO;
import com.api.pmtool.dtos.SearchDemandResponseDto;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.entity.ProjectEntity;
import com.api.pmtool.entity.Uploads;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.exception.FileStorageException;
import com.api.pmtool.exception.ResourceNotFoundException;
import com.api.pmtool.repository.DemandRepository;
import com.api.pmtool.repository.ProjectRepository;
import com.api.pmtool.repository.UploadRepository;
import com.api.pmtool.repository.UserRepository;
import org.apache.tika.Tika;
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
    private UploadRepository uploadRepository;

    private static final String UPLOAD_DIRECTORY = "/Users/shyamrajsingh/Development/upload/"; // Root directory for
                                                                                               // file storage
    private final Path UPLOAD_DIR = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();
    private static final Logger logger = LoggerFactory.getLogger(DemandService.class);

    /**
     * Creates a new Demand, assigns it an initial status of NOT_STARTED, adds a
     * Comment if provided, and associates the Comment with the Demand. If file
     * uploads are provided, it saves the files and associates them with the
     * Comment. Finally, it saves the Demand and returns the persisted Demand
     * entity.
     * 
     * @param demandRequestDto
     *                         The CreateDemandRequestDto containing the request
     *                         data
     * @return The persisted Demand entity
     * @throws IOException
     *                     If there is an error while saving the Demand
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
        demand.setDueDate(demandRequestDto.getDueDate());
        demand.setStatus(Status.NOT_STARTED);

        // Create and set the Comment
        Comments comment = new Comments();
        comment.setComment(demandRequestDto.getComments());
        comment.setDemand(demand); // Set bidirectional relationship with Demand

        // Associate the Comment with Demand
        demand.setComments(Collections.singletonList(comment));
        List<MultipartFile> files = demandRequestDto.getFiles();
        // Handle file uploads
        if (files != null && files.size() > 0) {
            List<Uploads> uploadsList = new ArrayList<>();
            for (MultipartFile file : files) {
                String filePath = saveFile(file);
                Uploads upload = new Uploads();
                upload.setComment(comment); // Associate upload with comment
                upload.setFileName(file.getOriginalFilename());
                upload.setFilePath(filePath);
                uploadsList.add(upload);
            }
            comment.setUploads(uploadsList); // Set bidirectional relationship for Uploads
        }

        // Save Demand, which cascades and saves Comments and Uploads due to
        // CascadeType.ALL
        return demandRepository.save(demand);
    }

    /**
     * Assigns users to specific roles within a Demand and optionally adds a comment
     * with file uploads.
     * 
     * This method fetches a Demand by the given demandId from the request DTO and
     * assigns users to roles
     * such as Manager, Tech Lead, and Developers. If the comment and files are
     * provided, they are associated
     * with the Demand. The entire operation is executed within a transaction to
     * ensure consistency.
     *
     * @param dto An AssignDemandRequestDto containing the demand ID, assignee IDs,
     *            and an optional comment
     *            with files.
     * @return The updated Demand entity after assigning roles and adding any
     *         comments.
     * @throws IOException              If an error occurs during file processing.
     * @throws IllegalArgumentException If the Demand or any user IDs specified in
     *                                  the DTO cannot be found.
     */
    @Override
    @Transactional // Ensure the operation happens in a single transaction
    public Demand assignDemand(AssignDemandRequestDto dto) throws IOException, IllegalArgumentException {
        // Fetch the demand by demandId
        Demand demand = demandRepository.findById(dto.getDemandId())
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + dto.getDemandId()));

        // Create a new map and populate it with new user roles
        Map<User, String> newUserRoles = new HashMap<>();
        // Assign Manager (MANAGER role)
        User manager = userRepository.findById(dto.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + dto.getAssigneeId()));
        newUserRoles.put(manager, "MANAGER");

        // Assign Tech Lead (TECH_LEAD role)
        User techLead = userRepository.findById(dto.getTechLeadId())
                .orElseThrow(() -> new IllegalArgumentException("Tech Lead not found with ID: " + dto.getTechLeadId()));
        newUserRoles.put(techLead, "TECH_LEAD");

        // Assign Developers (DEVELOPER role)
        List<User> developers = userRepository.findAllById(dto.getDeveloperIds());
        if (developers.size() != dto.getDeveloperIds().size()) {
            throw new IllegalArgumentException("Some developers not found");
        }
        for (User developer : developers) {
            newUserRoles.put(developer, "DEVELOPER");
        }
        demand.setUserRoles(newUserRoles);
        demand.setPriority(dto.getPriority()); // Set priority
        demand.setAssignDate(LocalDate.now());
        // Associate the Comment with Demand
        if (dto.getComment() != null && !dto.getComment().isEmpty()) {
            Comments newComment = new Comments();
            newComment.setComment(dto.getComment());
            newComment.setDemand(demand); // Set the bidirectional relationship

            List<MultipartFile> files = dto.getMultipartFiles();
            newComment.setUploads(processFile(files, newComment)); // Set bidirectional relationship for uploads
            // Add the new comment to the list of existing comments
            List<Comments> currentComments = demand.getComments();
            if (currentComments == null) {
                currentComments = new ArrayList<>();
            }
            currentComments.add(newComment);
            demand.setComments(currentComments); // Update the comments list
        }
        // }

        // Save Demand, which will cascade and save Comments and Uploads due to
        return demandRepository.save(demand);
    }

    /**
     * Retrieves a Demand by its ID, including its Comments and Uploads.
     * 
     * @param demandId The ID of the Demand to retrieve
     * @return The Demand entity with its Comments and Uploads
     * @throws IllegalArgumentException If the Demand is not found
     */
    @Override
    @Transactional
    public Demand getDemandByDemandId(UUID demandId) throws IllegalArgumentException {
        // Fetch the demand by its ID, including the comments and their uploads
        Demand demand = demandRepository.findByIdWithCommentsAndUploads(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // Initialize comments and uploads (to handle lazy loading)
        demand.getUserRoles().size();
        demand.getProject();
        demand.getStatusJourney().size();
        // demand.getComments().forEach(comment -> comment.getUploads().size());
        demand.getComments().forEach(comment -> {
            // Loop through uploads within each comment
            comment.getUploads().forEach(upload -> {
                // Remove unnecessary fields
                upload.setCreatedBy(null);
                upload.setCreatedAt(null);
                // upload.setModifiedBy(null);
                // upload.setModifiedAt(null);
                // upload.setFileName(null);  // Set fileName to null
                upload.setFilePath(null);  // Set filePath to null
            });
        });
        return demand;

    }

    // Change the due date, add comments, and handle file storage
    @Override
    @Transactional
    public void changeDueDate(ChangeDueDateRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getComment(), dto.getMultipartFiles(),
                dto.getNewDueDate(), null, null);
    }

    /**
     * Updates the status of the demand and adds a comment if provided. If files
     * are provided, it saves the files and associates them with the comment.
     * 
     * @param dto The ChangeDemandStatusRequestDto containing the request data
     * @throws IOException
     *                     If there is an error while saving the Demand
     */

    @Override
    @Transactional
    public void changeDemandStatus(ChangeDemandStatusRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getComment(), dto.getMultipartFiles(), null,
                dto.getNewStatus(), null);
    }

    @Transactional
    @Override
    public void changePriority(ChangePriorityRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getComment(), dto.getMultipartFiles(), null,
                null, dto.getNewPriority());
    }

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
     * Adds a comment to a Demand, optionally with file uploads.
     * 
     * @param dto The AddCommentOnDemandDto containing the request data
     * @throws IOException
     *                     If there is an error while saving the Demand
     */
    @Override
    @Transactional
    public void addCommentOnDemand(AddCommentOnDemandDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(), dto.getComment(), dto.getMultipartFiles(), null, null,
                null);
    }

    /**
     * Updates the Demand details and adds a comment if provided. If the due date is
     * provided, it updates the due date and
     * increments the dueDateChangeCount. If a new status is provided, it updates
     * the status of the demand. If comment is
     * provided, it adds a new comment to the Demand and associates it with the
     * demand. If files are provided, it saves the
     * files and associates them with the new comment. The entire operation is
     * executed within a transaction to ensure
     * consistency.
     * 
     * @param demandId
     *                   The ID of the Demand to update
     * @param comment
     *                   The comment to add
     * @param files
     *                   The files to save and associate with the comment
     * @param newDueDate
     *                   The new due date to set
     * @param status
     *                   The new status to set
     * @throws IOException
     *                     If there is an error while saving the Demand
     */
    private void UpdateDemandDetailsAndAddComment(UUID demandId, @Nullable String comment,
            @Nullable List<MultipartFile> files,
            @Nullable LocalDate newDueDate, @Nullable Status status, @Nullable Priority newPriority)
            throws IOException {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // demand.getComments().forEach(comment -> comment.getUploads().size());
        demand.getUserRoles().size();
        if (newPriority != null) {
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.setPriority(newPriority);
        }
        if (newDueDate != null) {
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.extendDueDate(newDueDate);
        }
        if (status != null) {
            demand.setStatusChangeDate(LocalDate.now());
            demand.getStatusJourney().put(status, LocalDate.now());
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.setStatus(status); // Update the status of the demand with the new status;
        }
        // Associate the Comment with Demand
        if (comment != null && !comment.isEmpty()) {
            Comments newComment = new Comments();
            newComment.setComment(comment);
            newComment.setDemand(demand); // Set the bidirectional relationship
            newComment.setUploads(processFile(files, newComment)); // Set bidirectional relationship for uploads
            // Add the new comment to the list of existing comments
            List<Comments> currentComments = demand.getComments();
            if (currentComments == null) {
                currentComments = new ArrayList<>();
            }
            currentComments.add(newComment);
            demand.setComments(currentComments); // Update the comments list
        }
        // Save the demand (this will cascade and save the new comment and upload as
        // well)
        demandRepository.save(demand);
    }

    /**
     * Processes a list of files by saving each file and associating it with a
     * comment.
     * Creates and returns a list of Uploads entities, each representing a saved
     * file.
     *
     * @param files       The list of files to process and save; nullable and can be
     *                    empty.
     * @param newComments The comment to associate with each file's upload;
     *                    nullable.
     * @return A list of Uploads entities associated with the given comment.
     * @throws IOException If an error occurs during file saving.
     */
    private List<Uploads> processFile(@Nullable List<MultipartFile> files, @Nullable Comments newComments)
            throws IOException {
        List<Uploads> uploadsList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filePath = saveFile(file);
                Uploads upload = new Uploads();
                upload.setComment(newComments); // Associate upload with the new comment
                upload.setFileName(file.getOriginalFilename());
                upload.setFilePath(filePath);
                uploadsList.add(upload);
            }
        }
        return uploadsList;
    }

    /**
     * Saves a file to the configured upload directory if it has valid file meta
     * data (i.e., it is not empty and has a valid original file name). If the
     * directory does not exist, it will be created. If the file already exists,
     * it will be overwritten.
     * <p>
     * The method returns the path to the saved file, or {@code null} if the file
     * was invalid.
     * 
     * @param file The file to be saved.
     * @return The path to the saved file, or {@code null} if the file was invalid.
     * @throws IOException If an error occurs while saving the file.
     */
    private String saveFile(MultipartFile file) throws IOException {
        if (!isValidFileMetaData(file)) {
            return null;
        }
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR); // This will create directories if they don't exist
        }
        String filePath = UPLOAD_DIR + "/" + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        // https://medium.com/@js_9757/secure-file-upload-api-with-springboot-1d1f415b80a6
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    /**
     * Validates the file meta data of the given {@code MultipartFile}.
     * This involves checking the MIME type of the file to ensure it is a
     * supported content type.
     * <p>
     * If the file has invalid meta data, a {@code FileStorageException} is
     * thrown.
     * 
     * @param file The file to be validated.
     * @return {@code true} if the file is valid, {@code false} otherwise.
     * @throws IOException If an error occurs while accessing the file data.
     */
    private boolean isValidFileMetaData(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        if (!isSupportedContentType(mimeType))
            throw new FileStorageException("File type not supported");
        return true;
    }

    /**
     * Determines whether the given MIME type is a supported content type.
     * The supported MIME types are:
     * 
     * application/pdf
     * image/png
     * image/jpg
     * image/jpeg
     * application/msword
     * application/vnd.openxmlformats-officedocument.wordprocessingml.document
     * 
     * 
     * @param contentType The MIME type to be checked.
     * @return {@code true} if the MIME type is supported, {@code false} otherwise.
     */
    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("application/pdf")
                || contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
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
    public Page<SearchDemandResponseDto> findDemandsByCriteria(String pfId, Status status, String role,
            String projectName,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // Create a Pageable object for pagination and sorting
        Page<SearchDemandResponseDto> demands = demandRepository.searchDemands(pfId, status, role, projectName,
                startDate, endDate, pageable);
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
