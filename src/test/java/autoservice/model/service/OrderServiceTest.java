package autoservice.model.service;

import autoservice.model.dto.response.OrderResponseDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.Master;
import autoservice.model.entities.Order;
import autoservice.model.entities.User;
import autoservice.model.enums.ActiveOrdersSortEnum;
import autoservice.model.enums.OrderStatus;
import autoservice.model.enums.OrdersSortByTimeFrameEnum;
import autoservice.model.enums.OrdersSortEnum;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.exceptions.PermissionException;
import autoservice.model.mapper.OrderMapper;
import autoservice.model.repository.GarageSpotRepository;
import autoservice.model.repository.MasterRepository;
import autoservice.model.repository.OrderRepository;
import autoservice.model.repository.UserRepository;
import autoservice.model.utils.PropertyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GarageSpotRepository garageSpotRepository;

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private PropertyUtil propertyUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void ordersSort_ShouldReturnListOfOrderResponseDtos_WhenByCreationDate() {
        Order o1 = new Order();
        o1.setCreatedAt(LocalDateTime.now().minusHours(1));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setCreatedAt(LocalDateTime.now().plusHours(2));
        o2.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o1);
        sortedOrders.add(o2);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByCreationDate(false)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.ordersSort(OrdersSortEnum.BY_CREATION_DATE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto1, orderResponseDtos.get(0));
        assertEquals(dto2, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByCreationDate(false);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSort_ShouldReturnListOfOrderResponseDtos_WhenByEndDate() {
        Order o1 = new Order();
        o1.setEndTime(LocalDateTime.now().plusHours(2));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setEndTime(LocalDateTime.now().minusHours(1));
        o2.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o2);
        sortedOrders.add(o1);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByEndDate(false)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.ordersSort(OrdersSortEnum.BY_END_DATE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto2, orderResponseDtos.get(0));
        assertEquals(dto1, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByEndDate(false);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSort_ShouldReturnListOfOrderResponseDtos_WhenByStartDate() {
        Order o1 = new Order();
        o1.setStartTime(LocalDateTime.now().plusHours(2));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setStartTime(LocalDateTime.now().minusHours(1));
        o2.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o2);
        sortedOrders.add(o1);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByStartDate()).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.ordersSort(OrdersSortEnum.BY_START_DATE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto2, orderResponseDtos.get(0));
        assertEquals(dto1, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByStartDate();
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSort_ShouldReturnListOfOrderResponseDtos_WhenByPrice() {
        Order o1 = new Order();
        o1.setPrice(BigDecimal.ONE);
        o1.setId(1L);
        Order o2 = new Order();
        o2.setPrice(BigDecimal.ZERO);
        o2.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o2);
        sortedOrders.add(o1);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByPrice(false)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.ordersSort(OrdersSortEnum.BY_PRICE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto2, orderResponseDtos.get(0));
        assertEquals(dto1, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByPrice(false);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSort_ShouldThrowIllegalArgumentException_WhenDecisionIsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.ordersSort(null));

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }


    @Test
    void activeOrdersSort_ShouldReturnListOfOrderResponseDtos_WhenByCreatingDate() {
        Order o1 = new Order();
        o1.setCreatedAt(LocalDateTime.now().minusHours(2));
        o1.setStartTime(LocalDateTime.now().minusHours(1));
        o1.setEndTime(LocalDateTime.now().plusHours(10));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setCreatedAt(LocalDateTime.now().minusHours(1));
        o2.setStartTime(LocalDateTime.now().minusHours(1));
        o2.setEndTime(LocalDateTime.now().plusHours(10));
        Order o3 = new Order();
        o3.setCreatedAt(LocalDateTime.now().plusHours(2));
        o3.setStartTime(LocalDateTime.now().plusHours(2));
        o2.setEndTime(LocalDateTime.now().plusHours(4));
        o3.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o1);
        sortedOrders.add(o2);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByCreationDate(true)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.activeOrdersSort(ActiveOrdersSortEnum.BY_CREATION_DATE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto1, orderResponseDtos.get(0));
        assertEquals(dto2, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByCreationDate(true);
        verify(mapper, times(2)).toDto(any(Order.class));
    }


    @Test
    void activeOrdersSort_ShouldReturnListOfOrderResponseDtos_WhenByEndDate() {
        Order o1 = new Order();
        o1.setCreatedAt(LocalDateTime.now().minusHours(2));
        o1.setStartTime(LocalDateTime.now().minusHours(1));
        o1.setEndTime(LocalDateTime.now().plusHours(11));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setCreatedAt(LocalDateTime.now().minusHours(1));
        o2.setStartTime(LocalDateTime.now().minusHours(1));
        o2.setEndTime(LocalDateTime.now().plusHours(10));
        Order o3 = new Order();
        o3.setCreatedAt(LocalDateTime.now().plusHours(2));
        o3.setStartTime(LocalDateTime.now().plusHours(2));
        o2.setEndTime(LocalDateTime.now().plusHours(12));
        o3.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o2);
        sortedOrders.add(o1);
        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByEndDate(true)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.activeOrdersSort(ActiveOrdersSortEnum.BY_END_DATE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto2, orderResponseDtos.get(0));
        assertEquals(dto1, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByEndDate(true);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void activeOrdersSort_ShouldReturnListOfOrderResponseDtos_WhenByPrice() {
        Order o1 = new Order();
        o1.setCreatedAt(LocalDateTime.now().minusHours(2));
        o1.setStartTime(LocalDateTime.now().minusHours(1));
        o1.setEndTime(LocalDateTime.now().plusHours(11));
        o1.setPrice(BigDecimal.ZERO);
        o1.setId(1L);
        Order o2 = new Order();
        o2.setCreatedAt(LocalDateTime.now().minusHours(1));
        o2.setStartTime(LocalDateTime.now().minusHours(1));
        o2.setEndTime(LocalDateTime.now().plusHours(10));
        o2.setPrice(BigDecimal.ONE);
        Order o3 = new Order();
        o3.setCreatedAt(LocalDateTime.now().plusHours(2));
        o3.setStartTime(LocalDateTime.now().plusHours(2));
        o2.setEndTime(LocalDateTime.now().plusHours(12));
        o3.setPrice(new BigDecimal(2));
        o3.setId(2L);
        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o1);
        sortedOrders.add(o2);

        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);
        given(orderRepository.ordersSortByPrice(true)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> orderResponseDtos = orderService.activeOrdersSort(ActiveOrdersSortEnum.BY_PRICE);

        assertEquals(2, orderResponseDtos.size());
        assertEquals(dto1, orderResponseDtos.get(0));
        assertEquals(dto2, orderResponseDtos.get(1));

        verify(orderRepository).ordersSortByPrice(true);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void activeOrdersSort_ShouldThrowIllegalArgumentException_WhenDecisionIsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.activeOrdersSort(null));

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void getOrderByMaster_ShouldReturnOrderResponseDto_WhenMasterExists() {
        Master master = new Master("denis", new BigDecimal(120.00));
        master.setId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setMaster(master);
        OrderResponseDto dto = new OrderResponseDto(1L, null, null, null, null, null, null, null);

        given(orderRepository.getOrderByMaster(master.getId())).willReturn(Optional.of(order));
        given(mapper.toDto(order)).willReturn(dto);

        OrderResponseDto result = orderService.getOrderByMaster(master.getId());

        assertEquals(dto, result);
        verify(mapper).toDto(order);
        verify(orderRepository).getOrderByMaster(master.getId());
    }

    @Test
    void getOrderByMaster_ShouldThrowNotFoundException_WhenMasterDoesNotExist() {
        given(orderRepository.getOrderByMaster(1L)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.getOrderByMaster(1L));

        verifyNoInteractions(mapper);
    }

    @Test
    void ordersSortByTimeFrame_ShouldReturnListOfOrderResponseDtos_WhenByCreationDate() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        Order o1 = new Order();
        o1.setCreatedAt(start.minusHours(2));
        o1.setStartTime(start.minusHours(1));
        o1.setEndTime(end.plusHours(1));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setCreatedAt(start.minusHours(1));
        o2.setStartTime(start.minusHours(1));
        o2.setEndTime(end.plusHours(1));
        o2.setId(2L);
        Order o3 = new Order();
        o3.setCreatedAt(start.plusHours(1));
        o3.setStartTime(start.plusHours(10));
        o3.setEndTime(end.plusHours(10));
        o3.setId(3L);

        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o1);
        sortedOrders.add(o2);

        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);


        given(orderRepository.ordersSortByTimeFrameByCreationDate(start, end)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> result = orderService.ordersSortByTimeFrame(start, end, OrdersSortByTimeFrameEnum.BY_CREATION_DATE);

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(orderRepository).ordersSortByTimeFrameByCreationDate(start, end);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSortByTimeFrame_ShouldReturnListOfOrderResponseDtos_WhenByEndDate() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);

        Order o1 = new Order();
        o1.setStartTime(start.minusHours(1));
        o1.setEndTime(end.plusHours(1));
        o1.setId(1L);
        Order o2 = new Order();
        o2.setStartTime(start.minusHours(1));
        o2.setEndTime(end.plusHours(2));
        o2.setId(2L);
        Order o3 = new Order();
        o3.setStartTime(start.plusHours(10));
        o3.setEndTime(end.plusHours(10));
        o3.setId(3L);

        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o1);
        sortedOrders.add(o2);

        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);


        given(orderRepository.ordersSortByTimeFrameByEndDate(start, end)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> result = orderService.ordersSortByTimeFrame(start, end, OrdersSortByTimeFrameEnum.BY_END_DATE);

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(orderRepository).ordersSortByTimeFrameByEndDate(start, end);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSortByTimeFrame_ShouldReturnListOfOrderResponseDtos_WhenByPrice() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);

        Order o1 = new Order();
        o1.setStartTime(start.minusHours(1));
        o1.setEndTime(end.plusHours(1));
        o1.setPrice(BigDecimal.ONE);
        o1.setId(1L);
        Order o2 = new Order();
        o2.setStartTime(start.minusHours(1));
        o2.setEndTime(end.plusHours(2));
        o2.setPrice(BigDecimal.ZERO);
        o2.setId(2L);
        Order o3 = new Order();
        o3.setStartTime(start.plusHours(10));
        o3.setEndTime(end.plusHours(10));
        o3.setPrice(BigDecimal.ZERO);
        o3.setId(3L);

        List<Order> sortedOrders = new ArrayList<>();
        sortedOrders.add(o2);
        sortedOrders.add(o1);

        OrderResponseDto dto1 = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        OrderResponseDto dto2 = new OrderResponseDto(2L, null, null, null, null, null, null, null);


        given(orderRepository.ordersSortByTimeFrameByEndDate(start, end)).willReturn(sortedOrders);
        given(mapper.toDto(o1)).willReturn(dto1);
        given(mapper.toDto(o2)).willReturn(dto2);

        List<OrderResponseDto> result = orderService.ordersSortByTimeFrame(start, end, OrdersSortByTimeFrameEnum.BY_END_DATE);

        assertEquals(2, result.size());
        assertEquals(dto2, result.get(0));
        assertEquals(dto1, result.get(1));

        verify(orderRepository).ordersSortByTimeFrameByEndDate(start, end);
        verify(mapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void ordersSortByTimeFrame_ShouldThrowIllegalArgumentException_WhenDecisionIsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.ordersSortByTimeFrame(LocalDateTime.now(), LocalDateTime.now().plusHours(1), null));

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void ordersSortByTimeFrame_ShouldThrowIllegalArgumentException_WhenStartIsNull() {
        assertThrows(RuntimeException.class,
                () -> orderService.ordersSortByTimeFrame(null, LocalDateTime.now(), OrdersSortByTimeFrameEnum.BY_PRICE));

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }


    @Test
    void addOrder_ShouldReturnOrderResponseDto_WhenUserExists() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        GarageSpot spot = new GarageSpot(); spot.setId(1L);
        Master master = new Master(); master.setId(10L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of(master));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        OrderResponseDto expectedDto = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));
        given(mapper.toDto(any(Order.class))).willReturn(expectedDto);

        OrderResponseDto result = orderService.addOrder("Oil Change", 2, new BigDecimal("100"), user);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(orderRepository).save(any(Order.class));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void addOrder_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        User user = new User();
        user.setUsername("name");
        given(userRepository.findByUsername("name")).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.addOrder("asd", 2, new BigDecimal(1), user));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrder_ShouldThrowRuntimeException_WhenMastersDontExist() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        GarageSpot spot = new GarageSpot(); spot.setId(1L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of());
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());


        assertThrows(RuntimeException.class,
                () -> orderService.addOrder("Oil Change", 2, new BigDecimal("100"), user));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrderAtCurrentTime_ShouldReturnOrderResponseDto_WhenUserExists() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        GarageSpot spot = new GarageSpot(); spot.setId(1L);
        Master master = new Master(); master.setId(10L);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of(master));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        OrderResponseDto expectedDto = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));
        given(mapper.toDto(any(Order.class))).willReturn(expectedDto);

        OrderResponseDto result = orderService.addOrderAtCurrentTime(LocalDateTime.now(), "Oil Change", 2, new BigDecimal("100"), user);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void addOrderAtCurrentTime_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        User user = new User();
        user.setUsername("name");
        given(userRepository.findByUsername("name")).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.addOrderAtCurrentTime(LocalDateTime.now(), "asd", 2, new BigDecimal(1), user));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }


    @Test
    void addOrderAtCurrentTime_ShouldThrowRuntimeException_WhenMastersDontExist() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        GarageSpot spot = new GarageSpot(); spot.setId(1L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of());
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());


        assertThrows(RuntimeException.class,
                () -> orderService.addOrderAtCurrentTime(LocalDateTime.now(), "Oil Change", 2, new BigDecimal("100"), user));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrderWithCurrentMaster_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        User user = new User();
        user.setUsername("name");
        given(userRepository.findByUsername("name")).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.addOrderWithCurrentMaster("asd", 2, new BigDecimal(1), 1L, user));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrderWithCurrentMaster_ShouldThrowNotFoundException_WhenMasterDoesNotExist() {
        User user = new User();
        user.setUsername("name");
        given(userRepository.findByUsername("name")).willReturn(Optional.of(user));
        given(masterRepository.findById(1L)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> orderService.addOrderWithCurrentMaster("asd", 2, new BigDecimal(1), 1L, user));
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrderWithCurrentMaster_ShouldThrowRuntimeException_WhenGarageSpotsDontExist() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        Master master = new Master();
        master.setId(1L);
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(masterRepository.findById(1L)).willReturn(Optional.of(master));
        given(garageSpotRepository.findAll()).willReturn(List.of());
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.addOrderWithCurrentMaster("Oil Change", 2, new BigDecimal("100"), 1L, user));
        assertEquals("В системе отсувствует мастер или парковочное место", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void addOrderWithCurrentMaster_ShouldReturnOrderResponseDto() {
        User user = new User();
        user.setUsername("name");
        Master master = new Master();
        master.setId(1L);
        GarageSpot spot = new GarageSpot();
        spot.setId(1L);
        OrderResponseDto dto = new OrderResponseDto(1L, null, null, null, null, null, null, null);
        given(userRepository.findByUsername("name")).willReturn(Optional.of(user));
        given(masterRepository.findById(1L)).willReturn(Optional.of(master));
        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(mapper.toDto(any())).willReturn(dto);

        OrderResponseDto result = orderService.addOrderWithCurrentMaster("Oil Change", 2, new BigDecimal("100"), 1L, user);
        assertNotNull(result);
        assertEquals(dto, result);
        verify(orderRepository).save(any(Order.class));
        verify(mapper).toDto(any(Order.class));
    }

    @Test
    void deleteOrder_ShouldDeleteOrder_WhenItIsAllowed() {
        Long id = 1L;
        given(propertyUtil.isOrderAllowToDelete()).willReturn(true);

        orderService.deleteOrder(id);

        verify(orderRepository).delete(id);
    }

    @Test
    void deleteOrder_ShouldThrowPermissionException_WhenItIsNotAllowed() {
        given(propertyUtil.isOrderAllowToDelete()).willReturn(false);

        assertThrows(PermissionException.class,
                () -> orderService.deleteOrder(1L));

        verifyNoInteractions(orderRepository);
    }

    @Test
    void getOrders_ShouldReturnListOfOrders() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setEndTime(LocalDateTime.now().minusDays(1));
        o1.setOrderStatus(OrderStatus.CLOSED);
        Order o2 = new Order();
        o2.setEndTime(LocalDateTime.now().plusDays(2));
        o2.setId(2L);

        given(orderRepository.findAll()).willReturn(List.of(o1, o2));

        List<Order> orders = orderService.getOrders();

        assertEquals(2, orders.size());
        assertEquals(o1, orders.get(0));
        assertEquals(o2, orders.get(1));
    }

    @Test
    void getOrderById_ShouldReturnOrderResponseDto_WhenOrderExists() {
        Order o = new Order();
        o.setId(1L);
        o.setEndTime(LocalDateTime.now().minusDays(1));
        o.setOrderStatus(OrderStatus.OPEN);
        OrderResponseDto dto = new OrderResponseDto(1L, null, null, null, null, null, OrderStatus.OPEN, null);
        given(orderRepository.findById(1L)).willReturn(Optional.of(o));
        given(mapper.toDto(any())).willReturn(dto);
        OrderResponseDto result = orderService.getOrderById(1L);
        assertEquals(dto, result);
        verify(mapper).toDto(any(Order.class));
    }

    @Test
    void getOrderById_ShouldThrowNotFoundException_WhenOrderDoesNotExist() {
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.getOrderById(1L));

        assertEquals("Order with id=1 not found", notFoundException.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void getOrderByIdImport_ShouldReturnOrder_WhenOrderExists() {
        Order o = new Order();
        o.setId(1L);
        o.setEndTime(LocalDateTime.now().minusDays(1));
        o.setOrderStatus(OrderStatus.OPEN);
        given(orderRepository.findById(1L)).willReturn(Optional.of(o));

        Order result = orderService.getOrderByIdImport(1L);
        assertEquals(o, result);
    }

    @Test
    void getOrderByIdImport_ShouldReturnNull_WhenOrderDoesNotExist() {
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        Order orderById = orderService.getOrderByIdImport(1L);

        assertNull(orderById);
    }

    @Test
    void closeOrder_ShouldCloseOrder_WhenOrderExistsAndNotClosed() {
        Order o = new Order();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.OPEN);

        given(orderRepository.findById(1L)).willReturn(Optional.of(o));

        orderService.closeOrder(1L);
        assertEquals(OrderStatus.CLOSED, o.getOrderStatus());

    }

    @Test
    void closeOrder_ShouldThrowIllegalArgumentException_WhenOrderExistAndClosed() {
        Order o = new Order();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.CLOSED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(o));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.closeOrder(1L));
    }

    @Test
    void closeOrder_ShouldThrowNotFoundException_WhenOrderDoesNotExist() {
        given(orderRepository.findById(1L)).willReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.closeOrder(1L));
        assertEquals("Order with id=1 not found", notFoundException.getMessage());
    }

    @Test
    void cancelOrder_ShouldCancelOrder_WhenOrderExistsAndNotCanceled() {
        Order o = new Order();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.OPEN);

        given(orderRepository.findById(1L)).willReturn(Optional.of(o));

        orderService.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELLED, o.getOrderStatus());
    }

    @Test
    void cancelOrder_ShouldThrowIllegalArgumentException_WhenOrderExistAndCanceled() {
        Order o = new Order();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.CANCELLED);
        given(orderRepository.findById(1L)).willReturn(Optional.of(o));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_ShouldThrowNotFoundException_WhenOrderDoesNotExist() {
        given(orderRepository.findById(1L)).willReturn(Optional.empty());
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.cancelOrder(1L));
        assertEquals("Order with id=1 not found", notFoundException.getMessage());
    }

    @Test
    void shiftOrder_ShouldShiftChainOfOrders_WhenConflictsExist() {
        LocalDateTime now = LocalDateTime.now();

        Order source = new Order();
        source.setId(1L);
        source.setStartTime(now);
        source.setEndTime(now.plusHours(2));
        Master master1 = new Master();
        master1.setId(10L);
        GarageSpot spot1 = new GarageSpot();
        spot1.setId(100L);
        source.setMaster(master1);
        source.setGarageSpot(spot1);
        Order candidate = new Order();
        candidate.setId(2L);
        candidate.setStartTime(now.plusHours(2));
        candidate.setEndTime(now.plusHours(3));
        candidate.setMaster(master1);
        candidate.setGarageSpot(new GarageSpot());
        candidate.getGarageSpot().setId(200L);

        List<Order> orders = new ArrayList<>(List.of(source, candidate));
        given(propertyUtil.isOrderAllowToShiftTime()).willReturn(true);
        given(orderRepository.findAll()).willReturn(orders);
        orderService.shiftOrder(1L, 1);

        assertEquals(now.plusHours(3), source.getEndTime());
        assertEquals(now.plusHours(3), candidate.getStartTime());
        assertEquals(now.plusHours(4), candidate.getEndTime());
    }

    @Test
    void shiftOrder_ShouldShiftOrder_WhenConflictsDoNotExist() {
        LocalDateTime now = LocalDateTime.now();

        Order source = new Order();
        source.setId(1L);
        source.setStartTime(now);
        source.setEndTime(now.plusHours(2));
        Master master1 = new Master();
        master1.setId(10L);
        GarageSpot spot1 = new GarageSpot();
        spot1.setId(100L);
        source.setMaster(master1);
        source.setGarageSpot(spot1);
        Order candidate = new Order();
        candidate.setId(2L);
        candidate.setStartTime(now.plusHours(20));
        candidate.setEndTime(now.plusHours(30));
        candidate.setMaster(master1);
        candidate.setGarageSpot(new GarageSpot());
        candidate.getGarageSpot().setId(200L);

        List<Order> orders = new ArrayList<>(List.of(source, candidate));
        given(propertyUtil.isOrderAllowToShiftTime()).willReturn(true);
        given(orderRepository.findAll()).willReturn(orders);
        orderService.shiftOrder(1L, 1);

        assertEquals(now.plusHours(3), source.getEndTime());
        assertEquals(now.plusHours(20), candidate.getStartTime());
        assertEquals(now.plusHours(30), candidate.getEndTime());
    }

    @Test
    void shiftOrder_ShouldThrowPermissionException_WhenOItIsNotAllowed() {
        given(propertyUtil.isOrderAllowToShiftTime()).willReturn(false);
        assertThrows(PermissionException.class,
                () -> orderService.shiftOrder(1L, 1));
    }
}
