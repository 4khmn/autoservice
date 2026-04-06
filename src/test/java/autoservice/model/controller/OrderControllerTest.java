package autoservice.model.controller;

import autoservice.model.dto.create.OrderCreateDto;
import autoservice.model.dto.response.MasterResponseDto;
import autoservice.model.dto.response.OrderResponseDto;
import autoservice.model.enums.ActiveOrdersSortEnum;
import autoservice.model.enums.OrdersSortByTimeFrameEnum;
import autoservice.model.enums.OrdersSortEnum;
import autoservice.model.exceptions.GlobalExceptionHandler;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.service.MasterService;
import autoservice.model.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private MasterService masterService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getOrderById_ShouldReturnDto() throws Exception {
        OrderResponseDto dto = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        given(orderService.getOrderById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrderById_ShouldReturn404_WhenNotFound() throws Exception {
        given(orderService.getOrderById(999L))
                .willThrow(new NotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_WithoutDateAndMaster_ShouldReturnCreated() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(null, null, 1, null, null);
        OrderResponseDto responseDto = new OrderResponseDto(1L, null, null, null, null, null, null, null);

        given(orderService.addOrder(
                nullable(String.class),
                anyInt(),
                nullable(BigDecimal.class),
                any()
        )).willReturn(responseDto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
        verify(orderService).deleteOrder(1L);
    }

    @Test
    void getMasterByOrderId_ShouldReturnMaster() throws Exception {
        MasterResponseDto masterDto = new MasterResponseDto(10L, "Master Name", BigDecimal.valueOf(500));
        given(masterService.getMasterByOrder(1L)).willReturn(masterDto);

        mockMvc.perform(get("/api/orders/1/master"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Master Name"));
    }

    @Test
    void getMasterByOrderId_ShouldReturn404_WhenNotFound() throws Exception {
        given(masterService.getMasterByOrder(1L)).willThrow(new NotFoundException("Master not found"));
        mockMvc.perform(get("/api/orders/1/master"))
                .andExpect(status().isNotFound());
    }

    @Test
    void closeOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/orders/1/close"))
                .andExpect(status().isNoContent());
        verify(orderService).closeOrder(1L);
    }

    @Test
    void shiftOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/orders/1/shift")
                        .param("duration", "2"))
                .andExpect(status().isNoContent());
        verify(orderService).shiftOrder(1L, 2);
    }

    @Test
    void getAllOrders_ShouldReturnSortedList() throws Exception {
        OrderResponseDto dto = new OrderResponseDto(1L, "Desc", null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null);
        given(orderService.ordersSort(OrdersSortEnum.BY_CREATION_DATE)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/orders")
                        .param("sort", "BY_CREATION_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Desc"));
    }

    @Test
    void getActiveOrders_ShouldReturnSortedList() throws Exception {
        OrderResponseDto dto = new OrderResponseDto(1L, "Desc", null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, null);
        given(orderService.activeOrdersSort(ActiveOrdersSortEnum.BY_CREATION_DATE)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/active")
                        .param("sort", "BY_CREATION_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Desc"));
    }
    @Test
    void getOrdersHistory_ShouldReturnList() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        OrderResponseDto dto = new OrderResponseDto(1L, "History", null, null, null, null, null, null);

        given(orderService.ordersSortByTimeFrame(eq(start), eq(end), eq(OrdersSortByTimeFrameEnum.BY_CREATION_DATE)))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/api/orders/history")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("sort", "BY_CREATION_DATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("History"));
    }

    @Test
    void getOrdersHistory_ShouldReturn400_WhenStartIsMissing() throws Exception {
        mockMvc.perform(get("/api/orders/history")
                        .param("end", LocalDateTime.now().toString())
                        .param("sort", "BY_CREATION_DATE"))
                .andExpect(status().isBadRequest());
    }

}