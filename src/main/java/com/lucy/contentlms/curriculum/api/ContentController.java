package com.lucy.contentlms.curriculum.api;

import com.lucy.contentlms.curriculum.application.ContentImportService;
import com.lucy.contentlms.curriculum.application.CurriculumCommandService;
import com.lucy.contentlms.curriculum.application.CurriculumQueryService;
import com.lucy.contentlms.curriculum.application.dto.ImportSummaryResponse;
import com.lucy.contentlms.curriculum.application.dto.LessonLevelDetailResponse;
import com.lucy.contentlms.curriculum.application.dto.LessonLevelSummaryResponse;
import com.lucy.contentlms.curriculum.application.dto.SourceDocumentResponse;
import com.lucy.contentlms.curriculum.application.dto.UpsertLessonLevelRequest;
import com.lucy.contentlms.curriculum.domain.model.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api")
public class ContentController {

    private final CurriculumQueryService curriculumQueryService;
    private final ContentImportService contentImportService;
    private final CurriculumCommandService curriculumCommandService;

    public ContentController(
            CurriculumQueryService curriculumQueryService,
            ContentImportService contentImportService,
            CurriculumCommandService curriculumCommandService
    ) {
        this.curriculumQueryService = curriculumQueryService;
        this.contentImportService = contentImportService;
        this.curriculumCommandService = curriculumCommandService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "lucy-content-lms");
    }

    @GetMapping("/languages")
    public List<Language> languages() {
        return Arrays.asList(Language.values());
    }

    @GetMapping("/documents")
    public List<SourceDocumentResponse> documents() {
        return curriculumQueryService.documents();
    }

    @GetMapping("/levels")
    public List<LessonLevelSummaryResponse> levels(@RequestParam Language language) {
        return curriculumQueryService.levels(language);
    }

    @GetMapping("/levels/{id}")
    public ResponseEntity<LessonLevelDetailResponse> levelById(@PathVariable Long id) {
        return curriculumQueryService.levelById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/level-content")
    public List<LessonLevelDetailResponse> levelContent(
            @RequestParam Language language,
            @RequestParam @Min(1) @Max(100) int level
    ) {
        return curriculumQueryService.levelContent(language, level);
    }

    @GetMapping("/search")
    public List<LessonLevelSummaryResponse> search(@RequestParam String q) {
        return curriculumQueryService.search(q);
    }

    @PostMapping(value = "/imports/docx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportSummaryResponse importDocx(@RequestPart("files") List<MultipartFile> files) {
        return contentImportService.importUploadedDocuments(files);
    }

    @PostMapping("/levels")
    public ResponseEntity<LessonLevelDetailResponse> createLevel(@Valid @org.springframework.web.bind.annotation.RequestBody UpsertLessonLevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumCommandService.createLevel(request));
    }

    @PutMapping("/levels/{id}")
    public LessonLevelDetailResponse updateLevel(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpsertLessonLevelRequest request
    ) {
        return curriculumCommandService.updateLevel(id, request);
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        curriculumCommandService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }
}
