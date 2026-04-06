package autoservice.model.controller;

import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.service.io.imports.CsvImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DataImportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CsvImportService csvImportService;

    @InjectMocks
    private DataImportController dataImportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataImportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void importMasters_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/import/masters"))
                .andExpect(status().isNoContent());

        verify(csvImportService).importMasters();
    }

    @Test
    void importGarageSpots_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/import/garage-spots"))
                .andExpect(status().isNoContent());

        verify(csvImportService).importGarageSpots();
    }

    @Test
    void importOrders_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/import/orders"))
                .andExpect(status().isNoContent());

        verify(csvImportService).importOrders();
    }
}