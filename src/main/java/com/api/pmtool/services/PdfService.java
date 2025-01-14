package com.api.pmtool.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface PdfService {

public File mergePdfFiles(List<String> pdfPaths, String appId) throws IOException;

public PDDocument encryptPDF(File inputPdf, String userPassword, String ownerPassword) throws Exception;
}
