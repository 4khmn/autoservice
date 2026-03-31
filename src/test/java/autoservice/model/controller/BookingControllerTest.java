package autoservice.model.controller;

import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private BookingController bookingController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getFreeSpotsCount_ShouldReturnLong()  throws Exception {
        LocalDateTime date = LocalDateTime.now();
        given(scheduleService.getNumberOfFreeSpotsByDate(date)).willReturn(1L);

        mockMvc.perform(get("/api/schedule/free-spots")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getFreeSpotsCount_ShouldReturn400_WhenDateIsMissing()  throws Exception {
        mockMvc.perform(get("/api/schedule/free-spots"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClosestAvailableDate_ShouldReturnLocalDateTime()  throws Exception {
        LocalDateTime date = LocalDateTime.now().withNano(0);
        given(scheduleService.getClosestDate(1)).willReturn(date);

        mockMvc.perform(get("/api/schedule/closest-date")
                        .param("duration", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(date.toString()));
    }

    @Test
    void getClosestAvailableDate_ShouldReturn400_WhenDuration()  throws Exception {
        mockMvc.perform(get("/api/schedule/closest-date"))
                .andExpect(status().isBadRequest());
    }

}
