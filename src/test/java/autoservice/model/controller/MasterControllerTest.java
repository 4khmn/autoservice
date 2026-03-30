package autoservice.model.controller;

import autoservice.model.dto.create.MasterCreateDto;
import autoservice.model.dto.response.MasterResponseDto;
import autoservice.model.dto.response.OrderResponseDto;
import autoservice.model.entities.Master;
import autoservice.model.enums.MastersSortEnum;
import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.mapper.MasterMapper;
import autoservice.model.service.MasterService;
import autoservice.model.service.OrderService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MasterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MasterMapper mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MasterService masterService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private MasterController controller;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }


    @Test
    void getMasterById_ShouldReturnDto() throws Exception {
        MasterResponseDto dto = new MasterResponseDto(1L, "denis", BigDecimal.ONE);
        given(masterService.getMasterById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/masters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("denis"));
    }

    @Test
    void getMasterById_ShouldReturn404_WhenNotFound() throws Exception {
        long id = 999L;
        given(masterService.getMasterById(id))
                .willThrow(new NotFoundException("Master spot with id=999 not found"));

        mockMvc.perform(get("/api/masters/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMaster_ShouldReturnCreated() throws Exception {
        MasterCreateDto createDto = new MasterCreateDto("denis", BigDecimal.ONE);
        MasterResponseDto responseDto = new MasterResponseDto(1L, "denis", BigDecimal.ONE);

        given(masterService.addMaster(any(MasterCreateDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/masters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteMaster_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/masters/5"))
                .andExpect(status().isNoContent());
        verify(masterService).deleteMaster(5L);
    }

    @Test
    void getMasters_ShouldReturnList() throws Exception {
        Master m1 = new Master();
        m1.setId(1L);
        m1.setName("denis");
        Master m2 = new Master();
        m2.setId(2L);
        m2.setName("maxim");

        MasterResponseDto dto1 = new MasterResponseDto(1L, "denis", BigDecimal.ONE);
        MasterResponseDto dto2 = new MasterResponseDto(2L, "maxim", BigDecimal.ONE);
        given(masterService.getMasters()).willReturn(List.of(m1, m2));

        given(mapper.toDto(m1)).willReturn(dto1);
        given(mapper.toDto(m2)).willReturn(dto2);

        mockMvc.perform(get("/api/masters")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

    }

    @Test
    void getSortedMasters_ShouldReturnList() throws Exception {
        MasterResponseDto dto1 = new MasterResponseDto(1L, "denis", BigDecimal.ONE);
        MasterResponseDto dto2 = new MasterResponseDto(2L, "maxim", BigDecimal.ONE);
        given(masterService.mastersSort(MastersSortEnum.BY_NAME)).willReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/masters/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(MastersSortEnum.BY_NAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getActiveOrder_ShouldReturnOrder() throws Exception {
        OrderResponseDto dto = new OrderResponseDto(1L, null, null, null, null, null, null, null);

        given(orderService.getOrderByMaster(1L)).willReturn(dto);

        mockMvc.perform(get("/api/masters/1/active-order")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
