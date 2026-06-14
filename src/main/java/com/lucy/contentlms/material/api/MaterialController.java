package com.lucy.contentlms.material.api;

import com.lucy.contentlms.material.api.dto.MaterialDownloadResponse;
import com.lucy.contentlms.material.api.dto.MaterialResponse;
import com.lucy.contentlms.material.application.MaterialService;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public List<MaterialResponse> materials() {
        return materialService.listMaterials();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) @Size(max = 180) String title,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(materialService.uploadMaterial(file, title, authentication));
    }

    @GetMapping("/{id}/download")
    public MaterialDownloadResponse downloadMaterial(@PathVariable Long id) {
        return materialService.downloadMaterial(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id, Authentication authentication) {
        materialService.deleteMaterial(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
