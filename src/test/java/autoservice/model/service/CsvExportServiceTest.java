package autoservice.model.service;

import autoservice.model.service.io.exports.CsvExport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    private class TestCsvExport extends CsvExport {
        public TestCsvExport() {
            super("id,name", "test.csv");
        }
        @Override
        protected String formatEntity(Object entity) {
            return entity.toString();
        }
        @Override
        protected Iterable<?> getEntities() {
            return List.of("1,TestItem");
        }
    }

    @Test
    void export_ShouldCreateFileWithHeaderAndData() throws IOException {
        TestCsvExport exportService = new TestCsvExport();

        exportService.export();

        File file = new File("data/test.csv");
        assertTrue(file.exists());

        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals("id,name", lines.get(0));
        assertEquals("1,TestItem", lines.get(1));

        file.delete();
    }
}