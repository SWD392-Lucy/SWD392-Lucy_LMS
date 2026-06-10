package com.lucy.contentlms.curriculum.infrastructure.docx;

import com.lucy.contentlms.curriculum.domain.model.Language;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocxLessonParserTest {

    private final DocxLessonParser parser = new DocxLessonParser();

    @Test
    void parsesChineseLevelDocument() {
        ParsedDocument document = parser.parse(Path.of("doc", "Chinese - level 1-30.docx"));

        assertThat(document.language()).isEqualTo(Language.CHINESE);
        assertThat(document.levels()).hasSize(30);
        assertThat(document.levels().stream().map(ParsedLevel::levelNumber))
                .containsExactlyElementsOf(IntStream.rangeClosed(1, 30).boxed().toList());
        assertThat(document.levels().getFirst().levelNumber()).isEqualTo(1);
        assertThat(document.levels().getFirst().title()).isNotBlank();
        assertThat(document.levels().getFirst().blocks()).isNotEmpty();
    }

    @Test
    void parsesEnglishLevelDocument() {
        ParsedDocument document = parser.parse(Path.of("doc", "Eng - STAGE 1 (LEVELS 1-30).docx"));

        assertThat(document.language()).isEqualTo(Language.ENGLISH);
        assertThat(document.levels()).hasSize(30);
        assertThat(document.levels().stream().map(ParsedLevel::levelNumber))
                .containsExactlyElementsOf(IntStream.rangeClosed(1, 30).boxed().toList());
        assertThat(document.levels().getFirst().levelNumber()).isEqualTo(1);
        assertThat(document.levels().getFirst().title()).contains("SAYING WHO I AM");
    }

    @Test
    void parsesJapaneseStageThreeDocument() throws IOException {
        Path stageThreeDocument = Files.list(Path.of("doc"))
                .filter(path -> path.getFileName().toString().startsWith("Janpanes  - "))
                .findFirst()
                .orElseThrow();
        ParsedDocument document = parser.parse(stageThreeDocument);

        assertThat(document.language()).isEqualTo(Language.JAPANESE);
        assertThat(document.levels()).hasSize(11);
        assertThat(document.levels().getFirst().levelNumber()).isEqualTo(61);
        assertThat(document.levels().getFirst().durationMinutes()).isEqualTo(120);
        assertThat(document.levels().getLast().levelNumber()).isEqualTo(100);
    }

    @Test
    void parsesSecondChineseDocument() {
        ParsedDocument document = parser.parse(Path.of("doc", "Chinese - level 31-60.docx"));

        assertThat(document.levels()).hasSize(70);
        assertThat(document.levels().stream().map(ParsedLevel::levelNumber))
                .containsExactlyElementsOf(IntStream.rangeClosed(31, 100).boxed().toList());
    }
}
