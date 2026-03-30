package autoservice.model.controller;

import autoservice.model.dto.create.GarageSpotCreateDto;
import autoservice.model.dto.response.GarageSpotResponseDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.mapper.GarageSpotMapper;
import autoservice.model.service.GarageSpotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GarageSpotControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GarageSpotService garageSpotService;

    @Mock
    private GarageSpotMapper mapper;

    @InjectMocks
    private GarageSpotController garageSpotController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = mockMvc = MockMvcBuilders.standaloneSetup(garageSpotController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getGarageSpotById_ShouldReturnDto() throws Exception {
        GarageSpotResponseDto dto = new GarageSpotResponseDto(1L, 10.0, true, false);
        given(garageSpotService.getGarageSpotById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/garage-spots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.size").value(10.0));
    }

    @Test
    void getGarageSpotById_ShouldReturn404_WhenNotFound() throws Exception {
        long id = 999L;
        given(garageSpotService.getGarageSpotById(id))
                .willThrow(new NotFoundException("Garage spot with id=999 not found"));

        mockMvc.perform(get("/api/garage-spots/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGarageSpot_ShouldReturnCreated() throws Exception {
        GarageSpotCreateDto createDto = new GarageSpotCreateDto(15.0, false, true);
        GarageSpotResponseDto responseDto = new GarageSpotResponseDto(100L, 15.0, false, true);

        given(garageSpotService.addGarageSpot(any(GarageSpotCreateDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/garage-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void deleteGarageSpot_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/garage-spots/5"))
                .andExpect(status().isNoContent());
        verify(garageSpotService).deleteGarageSpot(5L);
    }

    @Test
    void getGarageSpots_ShouldReturnList() throws Exception {
        GarageSpot g1 = new GarageSpot();
        g1.setId(1L);
        g1.setSize(10.0);
        GarageSpot g2 = new GarageSpot();
        g2.setId(2L);
        g2.setSize(15.0);

        GarageSpotResponseDto dto1 = new GarageSpotResponseDto(1L, 10.0, true, false);
        GarageSpotResponseDto dto2 = new GarageSpotResponseDto(2L, 15.0, false, true);
        given(garageSpotService.getGarageSpots()).willReturn(List.of(g1, g2));

        given(mapper.toDto(g1)).willReturn(dto1);
        given(mapper.toDto(g2)).willReturn(dto2);

        mockMvc.perform(get("/api/garage-spots")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

    }

    @Test
    void getFreeGarageSpots_ShouldReturnList() throws Exception {
        GarageSpotResponseDto dto1 = new GarageSpotResponseDto(1L, 10.0, true, false);
        GarageSpotResponseDto dto2 = new GarageSpotResponseDto(2L, 15.0, false, true);

        given(garageSpotService.getFreeSpots()).willReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/garage-spots/free")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}