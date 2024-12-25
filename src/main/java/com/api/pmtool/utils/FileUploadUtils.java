package com.api.pmtool.utils;

import org.apache.tika.Tika;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.entity.CommentTypeEntity;
import com.api.pmtool.entity.Comments;
import com.api.pmtool.entity.Uploads;
import com.api.pmtool.exception.FileStorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUploadUtils {

    private static final String UPLOAD_DIRECTORY = "/Users/shyamrajsingh/Development/upload/"; // Root directory for
    // file storage
    private final Path UPLOAD_DIR = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();

  /*   @Value("${report.path}")
    private String reportPath; */
    /**
     * Validates the file meta data by checking its MIME type and file extension.
     * This method uses Apache Tika to detect the MIME type of the file and
     * compares it with the file extension to ensure consistency.
     * <p>
     * If the MIME type is not supported or if the extension does not match
     * the MIME type, a {@code FileStorageException} is thrown.
     * 
     * @param file The file to validate.
     * @return {@code true} if the file has valid meta data.
     * @throws IOException          If an error occurs while reading the file input
     *                              stream.
     * @throws FileStorageException If the file type is not supported or
     *                              if the file extension does not match the MIME
     *                              type.
     */
    public boolean isValidFileMetaData(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());

        // Extract file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        // Check if the MIME type is supported
        if (!isSupportedContentType(mimeType)) {
            throw new FileStorageException("File type not supported");
        }

        // Check if the extension matches the MIME type
        if (!isExtensionMatchingMimeType(fileExtension, mimeType)) {
            throw new FileStorageException("File extension does not match MIME type");
        }

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
    public boolean isSupportedContentType(String contentType) {
        return contentType.equals("application/pdf")
                || contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    /**
     * Determines whether the given file extension matches the given MIME type.
     * The supported MIME types and their corresponding file extensions are:
     * If the MIME type is not supported, or if the extension does not match the
     * MIME type,
     * {@code false} is returned.
     * 
     * @param extension The file extension to be checked.
     * @param mimeType  The MIME type to be checked.
     * @return {@code true} if the extension matches the MIME type, {@code false}
     *         otherwise.
     */
    public static boolean isExtensionMatchingMimeType(String extension, String mimeType) {
        switch (mimeType) {
            case "application/pdf":
                return extension.equals("pdf");
            case "image/png":
                return extension.equals("png");
            case "image/jpg":
            case "image/jpeg":
                return extension.equals("jpg") || extension.equals("jpeg");
            case "application/msword":
                return extension.equals("doc");
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return extension.equals("docx");
            default:
                return false;
        }
    }

    /**
     * Extracts the file extension from the given file name.
     * 
     * @param filename The file name from which to extract the extension.
     * @return The file extension (everything after the last dot) in lower case.
     * @throws IllegalArgumentException If the file name does not contain a dot.
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("File extension not present");
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public void saveFile(List<MultipartFile> files, List<String> filePaths) throws IOException {
        // Save the files only after the transaction succeeds
        for (String filePath : filePaths) {
            MultipartFile file = files.get(filePaths.indexOf(filePath)); // Get the file
            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String getFilePath(MultipartFile file, CommentTypeEntity commentType) throws IOException {
        // Validate the file's metadata
        if (!isValidFileMetaData(file)) {
            throw new IllegalArgumentException("Invalid file metadata");
        }
        String orgFileName = file.getOriginalFilename();

        String fileName = commentType.getCommentTypeName() + "_"
                + ((orgFileName != null && orgFileName.length() < 6) ? orgFileName.substring(0, 5)
                        : file.getOriginalFilename());
        // Generate the file path
        String filePath = UPLOAD_DIR + "/" + fileName;

        // Ensure the directory exists
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR); // Create directories if not present
        }
        return filePath;
    }

    public  List<String> getFilesPath(@Nullable List<MultipartFile> files, @Nullable CommentTypeEntity commentType)
            throws IOException {
        List<String> filePaths = new ArrayList<>();
        if (files != null && files.size() > 0 && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filePath = getFilePath(file, commentType);
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }
    
    public List<Uploads> uploadsSetter(@Nullable List<MultipartFile> files, @Nullable Comments newComments)
            throws IOException {
        List<Uploads> uploadsList = new ArrayList<>();
        CommentTypeEntity commentType = newComments != null ? newComments.getCommentType() : null;
        if (files != null && files.size() > 0 && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filePath = getFilePath(file, commentType);
                Uploads upload = new Uploads();
                upload.setComment(newComments); // Associate upload with comment
                upload.setFilePath(filePath);
                uploadsList.add(upload);
            }
        }
        return uploadsList;
    }

    public ByteArrayOutputStream getFilesAsZip(List<Path> filePaths) throws IOException{
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
            for (Path filePath : filePaths) {
                if (Files.exists(filePath)) {
                    zos.putNextEntry(new ZipEntry(filePath.getFileName().toString()));
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                }
            }
        }
        return zipOutputStream;
    }
}
