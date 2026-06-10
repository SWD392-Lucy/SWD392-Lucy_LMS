-- Run this once if lesson_blocks.content was previously created as PostgreSQL oid
-- because LessonBlock.content used @Lob.
--
-- Check current type:
-- select column_name, data_type, udt_name
-- from information_schema.columns
-- where table_name = 'lesson_blocks' and column_name = 'content';
--
-- If udt_name = 'oid', convert stored large-object text into a normal text column.
alter table lesson_blocks
    alter column content type text
    using convert_from(lo_get(content), 'UTF8');
