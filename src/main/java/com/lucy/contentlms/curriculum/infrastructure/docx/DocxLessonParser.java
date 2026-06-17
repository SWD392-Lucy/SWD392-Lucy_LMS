package com.lucy.contentlms.curriculum.infrastructure.docx;

import com.lucy.contentlms.curriculum.domain.model.Language;
import com.lucy.contentlms.curriculum.domain.model.Stage;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class DocxLessonParser {

    private static final Pattern CHINESE_LEVEL = Pattern.compile("^(\\d{1,3})(?:\\s*[-\\u2013\\u2014]\\s*(\\d{1,3}))?[.\\uFF0E\\uFF1A:]\\s*(.+)$");
    private static final Pattern ENGLISH_LEVEL = Pattern.compile("(?i).*\\bLEVEL\\s+(\\d{1,3})(?:\\s*[-\\u2013\\u2014]\\s*(\\d{1,3}))?\\s*[-\\u2013\\u2014:\\uFF1A]\\s*(?!\\d)(.+)$");
    private static final Pattern JAPANESE_LEVEL = Pattern.compile(".*\\u30EC\\u30D9\\u30EB\\s*(\\d{1,3})(?:\\s*[-\\u2013\\u2014]\\s*(\\d{1,3}))?\\s*[-\\u2013\\u2014:\\uFF1A]\\s*(?!\\d)(.+)$");

    public ParsedDocument parse(Path docxPath) {
        String fileName = docxPath.getFileName().toString();
        try {
            return parse(java.nio.file.Files.newInputStream(docxPath), fileName);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read DOCX file: " + docxPath, exception);
        }
    }

    public ParsedDocument parse(InputStream inputStream, String fileName) {
        Language language = inferLanguage(fileName);
        String courseCode = inferCourseCode(language);
        List<String> paragraphs = extractParagraphs(inputStream, fileName).stream()
                .flatMap(text -> splitCombinedHeadings(language, text).stream())
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .toList();

        List<ParsedLevel> levels = new ArrayList<>();
        LevelDraft current = null;
        Heading currentRange = null;
        int nextLevelNumberInRange = -1;

        for (String paragraph : paragraphs) {
            Heading heading = parseHeading(language, paragraph);
            if (heading != null) {
                if (current != null) {
                    levels.add(current.toParsedLevel());
                    current = null;
                }
                if (heading.endLevelNumber() != null) {
                    currentRange = heading;
                    nextLevelNumberInRange = heading.levelNumber();
                } else {
                    currentRange = null;
                    current = new LevelDraft(
                            heading.levelNumber(),
                            heading.title(),
                            language,
                            inferStage(heading.levelNumber()),
                            courseCode,
                            inferDurationMinutes(heading.levelNumber())
                    );
                }
            } else {
                List<String> extractedTitles = new ArrayList<>();
                if (language == Language.ENGLISH) {
                    for (String line : paragraph.split("\\r?\\n")) {
                        String subLevelTitle = extractSubLevelTitle(language, line.trim());
                        if (subLevelTitle != null && !subLevelTitle.isBlank()) {
                            extractedTitles.add(subLevelTitle);
                        }
                    }
                } else {
                    String subLevelTitle = extractSubLevelTitle(language, paragraph);
                    if (subLevelTitle != null && !subLevelTitle.isBlank()) {
                        extractedTitles.add(subLevelTitle);
                    }
                }

                if (currentRange != null) {
                    for (String title : extractedTitles) {
                        if (nextLevelNumberInRange <= currentRange.endLevelNumber()) {
                            LevelDraft rangeLevel = new LevelDraft(
                                    nextLevelNumberInRange,
                                    title,
                                    language,
                                    inferStage(nextLevelNumberInRange),
                                    courseCode,
                                    inferDurationMinutes(nextLevelNumberInRange)
                            );
                            levels.add(rangeLevel.toParsedLevel());
                            nextLevelNumberInRange++;
                        }
                    }
                } else if (current != null) {
                    current.blocks().addAll(extractedTitles);
                }
            }
        }
        if (current != null) {
            levels.add(current.toParsedLevel());
        }
        return new ParsedDocument(fileName, language, courseCode, levels);
    }

    private List<String> splitCombinedHeadings(Language language, String text) {
        String regex = switch (language) {
            case CHINESE -> "(?<!\\d)(?=\\d{1,3}[.\\uFF0E]\\s+)";
            case ENGLISH -> "(?i)(?<!\\d)(?=\\bLEVEL\\s+\\d{1,3}\\s*[-\\u2013\\u2014]\\s*(?!\\d))";
            case JAPANESE -> "(?<!\\d)(?=\\u30EC\\u30D9\\u30EB\\s*\\d{1,3}\\s*[-\\u2013\\u2014]\\s*(?!\\d))";
        };
        String[] chunks = text.split(regex);
        List<String> result = new ArrayList<>();
        for (String chunk : chunks) {
            if (!chunk.isBlank()) {
                result.add(chunk);
            }
        }
        return result.isEmpty() ? List.of(text) : result;
    }

    private Heading parseHeading(Language language, String text) {
        Matcher matcher = switch (language) {
            case CHINESE -> CHINESE_LEVEL.matcher(text);
            case ENGLISH -> ENGLISH_LEVEL.matcher(text);
            case JAPANESE -> JAPANESE_LEVEL.matcher(text);
        };
        if (!matcher.matches()) {
            return null;
        }
        int levelNumber = Integer.parseInt(matcher.group(1));
        Integer endLevelNumber = null;
        if (matcher.group(2) != null) {
            endLevelNumber = Integer.parseInt(matcher.group(2));
        }
        if (levelNumber < 1 || levelNumber > 100) {
            return null;
        }
        String title = cleanTitle(matcher.group(3));
        return new Heading(levelNumber, endLevelNumber, title);
    }

    private String extractSubLevelTitle(Language language, String paragraph) {
        if (language == Language.ENGLISH) {
            Matcher matcher = Pattern.compile("(?i)^(?:Sub-level\\s*\\d+\\s*[:\\-]\\s*|\\d+[\\.\\:]\\s+)(.+)").matcher(paragraph);
            if (matcher.matches()) {
                return matcher.group(1).trim();
            }
        } else {
            Matcher matcher = Pattern.compile("(?i)^(?:Q\\d+\\s*[:\\-\\uFF1A]\\s*|\\d+[\\.\\:\\uFF1A]\\s+)(.+)").matcher(paragraph);
            if (matcher.matches()) {
                String title = matcher.group(1).trim();
                // If it contains newline or 👉, take only the first part
                if (title.contains("\n")) {
                    title = title.substring(0, title.indexOf("\n")).trim();
                }
                if (title.contains("👉")) {
                    title = title.substring(0, title.indexOf("👉")).trim();
                }
                return title;
            } else if (language == Language.CHINESE) {
                if (paragraph.contains("👉")) {
                    String title = paragraph;
                    if (title.contains("\n")) {
                        title = title.substring(0, title.indexOf("\n")).trim();
                    } else {
                        title = title.substring(0, title.indexOf("👉")).trim();
                    }
                    title = title.replaceFirst("^\\d+[\\.\\:\\uFF1A]\\s*", "");
                    return title;
                }
                
                // Fallback for Q+A in separate paragraphs: if it is a Chinese question
                if ((paragraph.endsWith("？") || paragraph.endsWith("?")) && paragraph.matches(".*[\\u4e00-\\u9fa5].*") && paragraph.length() <= 50) {
                    return paragraph.replaceFirst("^\\d+[\\.\\:\\uFF1A]\\s*", "").trim();
                }
            } else if (language == Language.JAPANESE) {
                // For Japanese, take the first line if it's short
                String firstLine = paragraph;
                if (firstLine.contains("\n")) {
                    firstLine = firstLine.substring(0, firstLine.indexOf("\n")).trim();
                }
                if (firstLine.length() > 0 && firstLine.length() <= 40 && !firstLine.contains("👉") && firstLine.matches(".*[\\p{L}\\p{N}].*")) {
                    return firstLine.replaceFirst("^\\s*[-•]\\s*", "").trim();
                }
            }
        }
        return null;
    }

    private String cleanTitle(String rawTitle) {
        return rawTitle
                .replaceFirst("^[\\s:：\\-\\u2013\\u2014]+", "")
                .trim();
    }

    private List<String> extractParagraphs(InputStream inputStream, String fileName) {
        String documentXml = readDocumentXml(inputStream, fileName);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(documentXml)));
            NodeList paragraphNodes = document.getElementsByTagNameNS(
                    "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
                    "p"
            );
            List<String> paragraphs = new ArrayList<>();
            for (int i = 0; i < paragraphNodes.getLength(); i++) {
                String text = collectText(paragraphNodes.item(i)).trim();
                if (!text.isBlank()) {
                    paragraphs.add(text);
                }
            }
            return paragraphs;
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse DOCX XML: " + fileName, exception);
        }
    }

    private String collectText(Node node) {
        StringBuilder builder = new StringBuilder();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("t".equals(child.getLocalName())) {
                builder.append(child.getTextContent());
            } else if ("tab".equals(child.getLocalName())) {
                builder.append(' ');
            } else if ("br".equals(child.getLocalName())) {
                builder.append('\n');
            } else {
                builder.append(collectText(child));
            }
        }
        return builder.toString();
    }

    private String readDocumentXml(InputStream inputStream, String fileName) {
        try (inputStream; ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    return new String(zipInputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read DOCX file: " + fileName, exception);
        }
        throw new IllegalStateException("DOCX file does not contain word/document.xml: " + fileName);
    }

    private Language inferLanguage(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.contains("chinese")) {
            return Language.CHINESE;
        }
        if (lower.contains("japanese") || lower.contains("janpanes")) {
            return Language.JAPANESE;
        }
        return Language.ENGLISH;
    }

    private String inferCourseCode(Language language) {
        return switch (language) {
            case ENGLISH -> "LISA_ENGLISH";
            case CHINESE -> "LUCY_CHINESE";
            case JAPANESE -> "LUCY_JAPANESE";
        };
    }

    private Stage inferStage(int levelNumber) {
        if (levelNumber <= 30) {
            return Stage.BEGINNER;
        }
        if (levelNumber <= 60) {
            return Stage.INTERMEDIATE;
        }
        return Stage.ADVANCED;
    }

    private int inferDurationMinutes(int levelNumber) {
        return levelNumber >= 61 ? 120 : 60;
    }

    private record Heading(int levelNumber, Integer endLevelNumber, String title) {
    }

    private record LevelDraft(
            int levelNumber,
            String title,
            Language language,
            Stage stage,
            String courseCode,
            int durationMinutes,
            List<String> blocks
    ) {
        LevelDraft(int levelNumber, String title, Language language, Stage stage, String courseCode, int durationMinutes) {
            this(levelNumber, title, language, stage, courseCode, durationMinutes, new ArrayList<>());
        }

        ParsedLevel toParsedLevel() {
            return new ParsedLevel(levelNumber, title, language, stage, courseCode, durationMinutes, List.copyOf(blocks));
        }
    }
}
