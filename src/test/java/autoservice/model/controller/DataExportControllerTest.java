package autoservice.model.controller;

import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.service.io.exports.GarageSpotsCsvExportService;
import autoservice.model.service.io.exports.MastersCsvExportService;
import autoservice.model.service.io.exports.OrdersCsvExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DataExportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MastersCsvExportService mastersExportService;

    @Mock
    private GarageSpotsCsvExportService garageSpotsExportService;

    @Mock
    private OrdersCsvExportService ordersExportService;

    @InjectMocks
    private DataExportController dataExportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataExportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void exportMasters_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/export/masters"))
                .andExpect(status().isOk());
        verify(mastersExportService).export();
    }

    @Test
    void exportGarageSpots_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/export/garage-spots"))
                .andExpect(status().isOk());

        verify(garageSpotsExportService).export();
    }

    @Test
    void exportOrders_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/export/orders"))
                .andExpect(status().isOk());
        verify(ordersExportService).export();
    }
}