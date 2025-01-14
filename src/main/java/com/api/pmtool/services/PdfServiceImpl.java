package com.api.pmtool.services;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import org.springframework.stereotype.Service;


@Service
public class PdfServiceImpl implements PdfService {

    @Override
    public File mergePdfFiles(List<String> pdfPaths, String appId) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        File mergedFile = new File("merged_" + appId + ".pdf");

        for (String path : pdfPaths) {
            merger.addSource(path);
        }
        merger.setDestinationFileName(mergedFile.getAbsolutePath());
        merger.mergeDocuments(null);

        return mergedFile;
    }


    @Override
    public PDDocument encryptPDF(File inputPdf, String userPassword, String ownerPassword) throws Exception {
        // Validate input
        if (inputPdf == null || userPassword == null || ownerPassword == null) {
            throw new IllegalArgumentException("Input file and passwords must not be null.");
        }

        // Load the PDF document
        PDDocument document = PDDocument.load(inputPdf);

        try {
            // Create access permission object
            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(false); // Disable printing
            ap.setCanExtractContent(false); // Disable content extraction

            // Create the protection policy
            StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap);
            spp.setEncryptionKeyLength(128); // Set encryption key length
            spp.setPermissions(ap); // Set permissions

            // Protect the document
            document.protect(spp);

            System.out.println("Document encrypted successfully.");
            return document; // Return the encrypted document
        } catch (Exception e) {
            throw e;
        }
    }
}
