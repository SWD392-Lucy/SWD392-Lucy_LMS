package com.lucy.contentlms.curriculum.application;

import com.lucy.contentlms.curriculum.application.dto.ImportSummaryResponse;
import com.lucy.contentlms.curriculum.domain.model.LessonBlock;
import com.lucy.contentlms.curriculum.domain.model.LessonLevel;
import com.lucy.contentlms.curriculum.domain.model.SourceDocument;
import com.lucy.contentlms.curriculum.infrastructure.docx.DocxLessonParser;
import com.lucy.contentlms.curriculum.infrastructure.docx.ParsedDocument;
import com.lucy.contentlms.curriculum.infrastructure.docx.ParsedLevel;
import com.lucy.contentlms.curriculum.infrastructure.persistence.SourceDocumentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ContentImportService {

    private final DocxLessonParser parser;
    private final SourceDocumentRepository sourceDocumentRepository;

    public ContentImportService(
            DocxLessonParser parser,
            SourceDocumentRepository sourceDocumentRepository
    ) {
        this.parser = parser;
        this.sourceDocumentRepository = sourceDocumentRepository;
    }

    @Transactional
    public ImportSummaryResponse importUploadedDocuments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one DOCX file must be uploaded");
        }

        int importedDocuments = 0;
        int skippedDocuments = 0;
        int importedLevels = 0;
        int importedBlocks = 0;
        for (MultipartFile file : files) {
            String fileName = cleanFileName(file.getOriginalFilename());
            validateDocxFile(file, fileName);
            if (sourceDocumentRepository.findByFileName(fileName).isPresent()) {
                skippedDocuments++;
                continue;
            }

            ParsedDocument parsedDocument = parseUploadedFile(file, fileName);
            SourceDocument sourceDocument = new SourceDocument(
                    parsedDocument.fileName(),
                    parsedDocument.language(),
                    parsedDocument.courseCode()
            );
            for (ParsedLevel parsedLevel : parsedDocument.levels()) {
                LessonLevel lessonLevel = new LessonLevel(
                        parsedLevel.levelNumber(),
                        parsedLevel.title(),
                        parsedLevel.language(),
                        parsedLevel.stage(),
                        parsedLevel.courseCode(),
                        parsedLevel.durationMinutes()
                );
                int sequenceNumber = 1;
                for (String block : parsedLevel.blocks()) {
                    lessonLevel.addBlock(new LessonBlock(sequenceNumber++, block));
                    importedBlocks++;
                }
                sourceDocument.addLevel(lessonLevel);
                importedLevels++;
            }
            sourceDocumentRepository.save(sourceDocument);
            importedDocuments++;
        }

        return new ImportSummaryResponse(importedDocuments, skippedDocuments, importedLevels, importedBlocks);
    }

    private ParsedDocument parseUploadedFile(MultipartFile file, String fileName) {
        try {
            return parser.parse(file.getInputStream(), fileName);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read uploaded DOCX file: " + fileName, exception);
        } catch (IllegalStateException exception) {
            throw new IllegalArgumentException("Cannot parse uploaded DOCX file: " + fileName, exception);
        }
    }

    private void validateDocxFile(MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty: " + fileName);
        }
        if (!fileName.toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("Only .docx files are supported: " + fileName);
        }
    }

    private String cleanFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Uploaded file must have a file name");
        }
        String normalized = originalFilename.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }
}
