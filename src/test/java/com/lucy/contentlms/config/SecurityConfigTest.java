package com.lucy.contentlms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucy.contentlms.curriculum.api.ContentController;
import com.lucy.contentlms.curriculum.application.ContentImportService;
import com.lucy.contentlms.curriculum.application.CurriculumCommandService;
import com.lucy.contentlms.curriculum.application.CurriculumQueryService;
import com.lucy.contentlms.curriculum.application.dto.ImportSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "lucy.security.jwt.signing-key=TEST_SIGNING_KEY_FOR_UNIT_TESTS_32B",
        "lucy.security.jwt.issuer=lucy.identity",
        "lucy.security.jwt.audience=lucy.clients"
})
class SecurityConfigTest {

    private static final String SIGNING_KEY = "TEST_SIGNING_KEY_FOR_UNIT_TESTS_32B";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurriculumQueryService curriculumQueryService;

    @MockBean
    private ContentImportService contentImportService;

    @MockBean
    private CurriculumCommandService curriculumCommandService;

    @BeforeEach
    void setUp() {
        when(contentImportService.importUploadedDocuments(anyList()))
                .thenReturn(new ImportSummaryResponse(1, 0, 5, 20));
    }

    @Test
    void publicReadEndpointRemainsAccessible() throws Exception {
        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk());
    }

    @Test
    void importRequiresAuthentication() throws Exception {
        mockMvc.perform(multipart("/api/imports/docx")
                        .file(docxFile())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importRejectsRoleWithoutMentorAccess() throws Exception {
        mockMvc.perform(multipart("/api/imports/docx")
                        .file(docxFile())
                        .header("Authorization", "Bearer " + token("Lucy", Instant.now().plusSeconds(900)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void importAcceptsProRole() throws Exception {
        mockMvc.perform(multipart("/api/imports/docx")
                        .file(docxFile())
                        .header("Authorization", "Bearer " + token("Pro", Instant.now().plusSeconds(900)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedDocuments").value(1));
    }

    @Test
    void importAcceptsSuperRole() throws Exception {
        mockMvc.perform(multipart("/api/imports/docx")
                        .file(docxFile())
                        .header("Authorization", "Bearer " + token("Super", Instant.now().plusSeconds(900)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedDocuments").value(1));
    }

    @Test
    void contentWriteRejectsRoleWithoutMentorAccess() throws Exception {
        mockMvc.perform(post("/api/levels")
                        .contentType("application/json")
                        .content("{}")
                        .header("Authorization", "Bearer " + token("Lucy", Instant.now().plusSeconds(900)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void contentWriteAllowsProRole() throws Exception {
        mockMvc.perform(post("/api/levels")
                        .contentType("application/json")
                        .content("{}")
                        .header("Authorization", "Bearer " + token("Pro", Instant.now().plusSeconds(900)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private MockMultipartFile docxFile() {
        return new MockMultipartFile(
                "files",
                "sample.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "fake-docx".getBytes(StandardCharsets.UTF_8)
        );
    }

    private String token(String role, Instant expiresAt) throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", UUID.randomUUID().toString());
        payload.put("displayName", "LMS Tester");
        payload.put("avatarPersona", "calm-blue");
        payload.put("isAnonymous", "Lucy".equals(role));
        payload.put("role", role);
        payload.put("iss", "lucy.identity");
        payload.put("aud", "lucy.clients");
        payload.put("iat", Instant.now().getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        ObjectMapper objectMapper = new ObjectMapper();
        String encodedHeader = base64Url(objectMapper.writeValueAsBytes(header));
        String encodedPayload = base64Url(objectMapper.writeValueAsBytes(payload));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return unsignedToken + "." + base64Url(sign(unsignedToken));
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SIGNING_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
