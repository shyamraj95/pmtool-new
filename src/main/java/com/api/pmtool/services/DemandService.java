package com.api.pmtool.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.dtos.AddCommentOnDemandDto;
import com.api.pmtool.dtos.AssignDemandRequestDto;
import com.api.pmtool.dtos.ChangeDueDateRequestDto;
import com.api.pmtool.dtos.CreateDemandRequestDto;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Demand;
import com.api.pmtool.entity.Uploads;
import com.api.pmtool.entity.User;
import com.api.pmtool.enums.Status;
import com.api.pmtool.exception.FileStorageException;
import com.api.pmtool.repository.DemandRepository;
import com.api.pmtool.repository.UserRepository;
import org.apache.tika.Tika;

@Service
public class DemandService {

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIRECTORY = "/Users/shyamrajsingh/Development/upload/"; // Root directory for
                                                                                               // file storage
    private final Path UPLOAD_DIR = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();

    @Transactional // Ensures that the entire method executes within a single transaction
    public Demand createDemand(CreateDemandRequestDto demandRequestDto) throws IOException {
        // Create a new Demand entity
        Demand demand = new Demand();
        demand.setProjectName(demandRequestDto.getProjectName());
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


    @Transactional
    public Demand getDemandByDemandId(UUID demandId) throws IllegalArgumentException {
        // Fetch the demand by its ID, including the comments and their uploads
        Demand demand = demandRepository.findByIdWithCommentsAndUploads(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // Initialize comments and uploads (to handle lazy loading)
        demand.getComments().forEach(comment -> comment.getUploads().size());
        demand.getUserRoles().size();
        return demand;

    }

    // Change the due date, add comments, and handle file storage
    @Transactional
    public void changeDueDate(ChangeDueDateRequestDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(),dto.getComment(), dto.getMultipartFiles(), dto.getNewDueDate());
/*         // Fetch the demand by demandId
        Demand demand = demandRepository.findById(dto.getDemandId())
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + dto.getDemandId()));
        // demand.getComments().forEach(comment -> comment.getUploads().size());
        demand.getUserRoles().size();
        // Extend due date using the existing method and increment dueDateChangeCount
        demand.extendDueDate(dto.getNewDueDate());
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
        // Save the demand (this will cascade and save the new comment and upload as
        // well)
        demandRepository.save(demand); */
    }
    @Transactional
    public void addCommentOnDemand(AddCommentOnDemandDto dto) throws IOException {
        UpdateDemandDetailsAndAddComment(dto.getDemandId(),dto.getComment(), dto.getMultipartFiles(), null);
    }
    // Helper function to update Demand and add comment
    private void UpdateDemandDetailsAndAddComment(UUID demandId, String comment, List<MultipartFile> files,
            @Nullable Date newDueDate) throws IOException {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found with ID: " + demandId));
        // demand.getComments().forEach(comment -> comment.getUploads().size());
        demand.getUserRoles().size();
        if (newDueDate != null) {
            // Extend due date using the existing method and increment dueDateChangeCount
            demand.extendDueDate(newDueDate);
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

    // Helper function to process file uploads
    private List<Uploads> processFile(List<MultipartFile> files, Comments newComments) throws IOException {
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

    // Method to save the uploaded file and return its path
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

    private boolean isValidFileMetaData(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        if (!isSupportedContentType(mimeType))
            throw new FileStorageException("File type not supported");
        return true;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("application/pdf")
                || contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
    //@Transactional
    public Page<Demand> findDemandsByCriteria(UUID userId, String userRole, String projectName, int page, int size, String sortBy, String sortDir) {
        // Create a Pageable object for pagination and sorting
        Pageable pageable = PageRequest.of(page, size, sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        // Call the repository method to search demands
        return demandRepository.searchDemands(userId, userRole, projectName, pageable);
    }
    
}

/*
 * 
 * // Helper method to create comments with file handling
 * private List<Comments> createComments(List<CommentRequestDto> commentDtos,
 * Demand demand, User commentedBy) {
 * List<Comments> commentsList = new ArrayList<>();
 * 
 * for (CommentRequestDto commentDto : commentDtos) {
 * Comments commentEntity = new Comments();
 * commentEntity.setDemand(demand);
 * commentEntity.setCommentedBy(commentedBy);
 * commentEntity.setComments(commentDto.getComment());
 * commentEntity.setUploadDate(LocalDateTime.now());
 * 
 * // Handle file upload (if provided)
 * if (commentDto.getFile() != null && !commentDto.getFile().isEmpty()) {
 * String filePath = storeFile(commentDto.getFile(), demand.getId());
 * commentEntity.setFilePath(filePath);
 * }
 * 
 * commentsList.add(commentEntity);
 * }
 * 
 * return commentsList;
 * }
 * 
 * // Method to handle file storage
 * private String storeFile(MultipartFile file, UUID demandId) {
 * try {
 * String demandDirectoryPath = "/path/to/files/" + demandId;
 * File demandDirectory = new File(demandDirectoryPath);
 * if (!demandDirectory.exists()) {
 * demandDirectory.mkdirs();
 * }
 * 
 * String currentDateTime =
 * LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
 * String fileName = demandId + "_" + currentDateTime + "_" +
 * file.getOriginalFilename();
 * String filePath = demandDirectoryPath + "/" + fileName;
 * 
 * File destFile = new File(filePath);
 * file.transferTo(destFile);
 * 
 * return filePath;
 * } catch (IOException e) {
 * throw new FileStorageException("Failed to store file", e);
 * }
 * }
 * private String getFileExtension(String fileName) {
 * if (fileName == null || fileName.lastIndexOf('.') == -1) {
 * return "";
 * }
 * return fileName.substring(fileName.lastIndexOf('.'));
 * }
 * 
 * // Utility method to find demand by ID
 * public Demand getDemandById(UUID demandId) {
 * return demandRepository.findById(demandId)
 * .orElseThrow(() -> new ResourceNotFoundException("Demand", "id", demandId));
 * }
 * 
 * 
 * public Page<Demand> findDemandsByUserOrDepartment(UUID assigneeId, UUID
 * techLeadId, String department, Pageable pageable) {
 * User assignee = assigneeId != null ? userRepository.findById(assigneeId)
 * .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId)) :
 * null;
 * 
 * User techLead = techLeadId != null ? userRepository.findById(techLeadId)
 * .orElseThrow(() -> new ResourceNotFoundException("User", "id", techLeadId)) :
 * null;
 * 
 * return demandRepository.findByAssigneeOrTechLeadOrDepartment(assignee,
 * techLead, department, pageable);
 * }
 * 
 * public Demand changeDemandStatus(UUID demandId, Status newStatus) {
 * Demand demand = getDemandById(demandId);
 * demand.setStatus(newStatus);
 * return demandRepository.save(demand);
 * }
 * public Demand assignDemandToUsers(AssignDemandRequestDto dto) {
 * Demand demand = demandRepository.findById(dto.getDemandId())
 * .orElseThrow(() -> new ResourceNotFoundException("Demand", "id",
 * dto.getDemandId()));
 * 
 * User assignee = userRepository.findById(dto.getAssigneeId())
 * .orElseThrow(() -> new ResourceNotFoundException("User", "id",
 * dto.getAssigneeId()));
 * 
 * User techLead = userRepository.findById(dto.getTechLeadId())
 * .orElseThrow(() -> new ResourceNotFoundException("User", "id",
 * dto.getTechLeadId()));
 * 
 * List<Developer> developers =
 * developerRepository.findAllById(dto.getDeveloperIds());
 * 
 * demand.setAssignee(assignee);
 * demand.setTechLead(techLead);
 * demand.setDevelopers(developers);
 * demand.setStatus(Status.IN_PROGRESS); // Set status to In Progress after
 * assignment
 * 
 * return demandRepository.save(demand);
 * }
 * 
 * // Method to create a new demand
 * 
 * // Update demand project and department
 * public Demand updateDemand(UUID demandId, UpdateDemandRequestDto dto) {
 * Demand demand = demandRepository.findById(demandId)
 * .orElseThrow(() -> new ResourceNotFoundException("Demand", "id", demandId));
 * 
 * demand.setProjectName(dto.getProjectName());
 * 
 * User assignee = demand.getAssignee();
 * if (assignee != null) {
 * assignee.setDepartment(dto.getDepartmentName());
 * }
 * 
 * return demandRepository.save(demand);
 * }
 * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
LocalDate newDueDate = LocalDate.parse("30/08/1988", formatter);
 * 
 */
