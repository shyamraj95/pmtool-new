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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUploadUtils {

    private static final String UPLOAD_DIRECTORY = "/Users/shyamrajsingh/Development/upload/"; // Root directory for


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
    public boolean isExtensionMatchingMimeType(String extension, String mimeType) {
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

    /**
     * Saves the given list of files to the file system at the given paths.
     * This method assumes that the given list of files and the given list of
     * paths are of the same length.
     * If the file already exists at the given path, it is replaced.
     *
     * @param files     The list of files to be saved.
     * @param filePaths The list of paths where the files are to be saved.
     * @throws IOException If an error occurs while writing to the file system.
     */
    public void saveFile(List<MultipartFile> files, List<String> filePaths, String canUploadMultipleFiles) throws IOException, FileAlreadyExistsException {
        // Save the files only after the transaction succeeds
        for (String filePath : filePaths) {
            MultipartFile file = files.get(filePaths.indexOf(filePath)); // Get the file
            Path path = Paths.get(filePath);
            // Check if the file already exists and if multi file upload is not allowed throw an exception
            if (canUploadMultipleFiles.equals("Y") && Files.exists(path)) {
                throw new FileAlreadyExistsException("File already exists: " + path); 
            }
            /* else if (canUploadMultipleFiles.equals("Y") &&Files.exists(path)) {
                String randomString = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 3);
                path = Paths.get(filePath + "_" + randomString);
            } */
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Generates a file path for the given file and comment type. The file path
     * is a concatenation of the upload directory and the comment type name
     * followed by the first 5 characters of the original file name. The method
     * also ensures that the upload directory exists, and creates it if not
     * present.
     * 
     * @param file        The file to generate a file path for. If
     *                    {@code null} or empty, the method will return {@code null}.
     * @param commentType The comment type to associate the file with. If
     *                    {@code null}, the method will not associate the file
     *                    with any comment type.
     * @return The file path for the given file and comment type, or
     *         {@code null} if the file is {@code null} or empty.
     * @throws IOException If an error occurs while reading the file input
     *                     stream.
     */
    public String getFilePath(MultipartFile file, CommentTypeEntity commentType, UUID demandId) throws IOException {
        // Validate the file's metadata
        if (!isValidFileMetaData(file)) {
            throw new IllegalArgumentException("Invalid file metadata");
        }
        String orgFileName = file.getOriginalFilename();

        String fileName = commentType.getCommentTypeName() + "_"
                + ((orgFileName != null && orgFileName.length() < 6) ? orgFileName.substring(0, 5)
                        : file.getOriginalFilename());
        // Generate the file path based on last part of demandId
        String demandFilesDir = UPLOAD_DIRECTORY + "/" + demandId.toString().split("-")[4];
 
        Path UPLOAD_DIR = Paths.get(demandFilesDir).toAbsolutePath().normalize();
        // Generate the file path
        String filePath = UPLOAD_DIR + "/" + fileName;

        // Ensure the directory exists
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR); // Create directories if not present
        }
        return filePath;
    }

    /**
     * Gets the list of file paths for the given list of files and comment type.
     * 
     * @param files        The list of files to get the file paths for. If
     *                     {@code null} or empty, the method will return an empty
     *                     list.
     * @param commentType  The comment type to associate the file with. If
     *                     {@code null}, the method will not associate the file
     *                     with any comment type.
     * @return A list of file paths, each of which is the full path to the
     *         uploaded file.
     * @throws IOException If an error occurs while reading the file input
     *                     stream.
     */
    public  List<String> getFilesPath(@Nullable List<MultipartFile> files, @Nullable CommentTypeEntity commentType, UUID demandId)
            throws IOException {
        List<String> filePaths = new ArrayList<>();
        if (files != null && files.size() > 0 && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filePath = getFilePath(file, commentType, demandId);
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }
    
    /**
     * Creates a list of Uploads objects from the given list of files and comment,
     * and associates each Uploads with the given comment.
     * 
     * @param files        The list of files to create Uploads objects for. If
     *                     {@code null} or empty, the method will return an empty
     *                     list.
     * @param newComments  The comment to associate the Uploads objects with. If
     *                     {@code null}, the method will not associate the Uploads
     *                     with any comment.
     * @return A list of Uploads objects, each of which is associated with the
     *         given comment.
     * @throws IOException If an error occurs while reading the file input
     *                     stream.
     */
    public List<Uploads> uploadsSetter(@Nullable List<MultipartFile> files, @Nullable Comments newComments, UUID demandId)
            throws IOException {
        List<Uploads> uploadsList = new ArrayList<>();
        CommentTypeEntity commentType = newComments != null ? newComments.getCommentType() : null;
        if (files != null && files.size() > 0 && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filePath = getFilePath(file, commentType, demandId);
                Uploads upload = new Uploads();
                upload.setComment(newComments); // Associate upload with comment
                upload.setFilePath(filePath);
                uploadsList.add(upload);
            }
        }
        return uploadsList;
    }

/**
 * Compresses a list of files into a ZIP format and returns the resulting byte array output stream.
 *
 * @param filePaths The list of file paths to be included in the ZIP archive. Each path should
 *                  correspond to an existing file.
 * @return A ByteArrayOutputStream containing the ZIP file data.
 * @throws IOException If an I/O error occurs while reading the files or writing to the ZIP output stream.
 */

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

        /**
     * Converts a MultipartFile to a temporary File.
     *
     * @param file MultipartFile to be converted
     * @return File object
     * @throws IOException If file operations fail
     */
    public File convertToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("uploaded", ".pdf");
        tempFile.deleteOnExit(); // Schedule the temp file for deletion when JVM exits
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile;
    }
}
