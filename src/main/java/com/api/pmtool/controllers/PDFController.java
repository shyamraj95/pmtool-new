package com.api.pmtool.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.pmtool.services.PdfService;
import com.api.pmtool.utils.FileUploadUtils;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {
    @Autowired
    private PdfService pdfService;
    @Autowired
    private FileUploadUtils fileUploadUtils;
    /**
     * Encrypts a PDF file and returns the encrypted file.
     *
     * @param file         The uploaded PDF file
     * @param userPassword The user password for encryption
     * @param ownerPassword The owner password for encryption
     * @return The encrypted PDF as a downloadable file
     */
    @PostMapping(path="/encrypt", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
public ResponseEntity<byte[]> encryptPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userPassword") String userPassword,
            @RequestParam("ownerPassword") String ownerPassword) {

        File tempInputFile = null;
        PDDocument encryptedDocument = null;

        try {
            // Convert MultipartFile to temporary File
            tempInputFile = fileUploadUtils.convertToFile(file);

            // Encrypt the PDF
            encryptedDocument = pdfService.encryptPDF(tempInputFile, userPassword, ownerPassword);

            // Write encrypted document to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            encryptedDocument.save(outputStream); // Save the document
            encryptedDocument.close(); // Close the document here

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "encrypted.pdf");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            // Ensure the temp file is deleted
            if (tempInputFile != null && tempInputFile.exists()) {
                tempInputFile.delete();
            }
            if (encryptedDocument != null) {
                try {
                    encryptedDocument.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Merges multiple PDF files into a single PDF and returns the merged file.
     *
     * @param pdfPaths List of file paths to be merged
     * @param appId    Unique application ID for the merged file
     * @return Merged PDF as a downloadable response
     */
    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfFiles(
            @RequestParam("pdfPaths") List<String> pdfPaths,
            @RequestParam("appId") String appId) {

        File mergedFile = null;

        try {
            // Call the service to merge the PDF files
            mergedFile = pdfService.mergePdfFiles(pdfPaths, appId);

            // Read the merged file into a byte array
            byte[] fileBytes = Files.readAllBytes(mergedFile.toPath());

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "merged_" + appId + ".pdf");

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            // Ensure the merged file is deleted after sending the response
            if (mergedFile != null && mergedFile.exists()) {
                mergedFile.delete();
            }
        }
    }
}