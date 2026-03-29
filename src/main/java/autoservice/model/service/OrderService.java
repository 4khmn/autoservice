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
import autoservice.model.service.domain.GarageSpotDomainService;
import autoservice.model.service.domain.MasterDomainService;
import autoservice.model.utils.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private transient final OrderRepository orderRepository;
    private transient final GarageSpotRepository garageSpotRepository;
    private transient final MasterRepository masterRepository;
    private transient final OrderMapper mapper;
    private final PropertyUtil propertyUtil;
    private final UserRepository userRepository;

    //4 список заказов
    @Transactional
    public List<OrderResponseDto> ordersSort(OrdersSortEnum decision) {
        List<Order> sortedOrders;
        switch (decision) {
            case BY_CREATION_DATE:
                //по дате подачи
                sortedOrders = orderRepository.ordersSortByCreationDate(false);
                break;
            case BY_END_DATE:
                //дата выполнения
                sortedOrders = orderRepository.ordersSortByEndDate(false);
                break;
            case BY_START_DATE:
                //дата планируемого начала выполнения
                sortedOrders = orderRepository.ordersSortByStartDate();
                break;
            case BY_PRICE:
                //по цене
                sortedOrders = orderRepository.ordersSortByPrice(false);
                break;
            default:
                //error
                throw new IllegalArgumentException("Неизвестный тип: " + decision);
        }
        return sortedOrders.stream()
                .map(mapper::toDto)
                .toList();
    }

    //4 список текущих выполняемых заказов
    @Transactional
    public List<OrderResponseDto> activeOrdersSort(ActiveOrdersSortEnum decision) {
        List<Order> sortedOrders;
        switch (decision) {
            case BY_CREATION_DATE:
                //по дате подачи
                sortedOrders = orderRepository.ordersSortByCreationDate(true);
                break;
            case BY_END_DATE:
                //по дате выполнения
                sortedOrders = orderRepository.ordersSortByEndDate(true);
                break;
            case BY_PRICE:
                //по цене
                sortedOrders = orderRepository.ordersSortByPrice(true);
                break;
            default:
                //error
                throw new IllegalArgumentException("Неизвестный тип: " + decision);
        }
        return sortedOrders.stream()
                .map(mapper::toDto)
                .toList();
    }

    //4 заказ, выполняемый конкретным мастером
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByMaster(Long masterId) {
        Order orderByMaster = orderRepository.getOrderByMaster(masterId).
                orElseThrow(() -> new NotFoundException("order by master with id " + masterId + " not found"));
        return mapper.toDto(orderByMaster);
    }

    //4 заказы (выполненные/удаленные/отмененные) за промежуток времени
    @Transactional(readOnly = true)
    public List<OrderResponseDto> ordersSortByTimeFrame(LocalDateTime start, LocalDateTime end, OrdersSortByTimeFrameEnum decision) {
        List<Order> ordersAtCurrentTime;
        switch (decision) {
            case BY_CREATION_DATE:
                //по дате подачи
                ordersAtCurrentTime = orderRepository.ordersSortByTimeFrameByCreationDate(start, end);
                break;
            case BY_END_DATE:
                //по дате выполнения
                ordersAtCurrentTime = orderRepository.ordersSortByTimeFrameByEndDate(start, end);
                break;
            case BY_PRICE:
                //по цене
                ordersAtCurrentTime = orderRepository.ordersSortByTimeFrameByPrice(start, end);
                break;
            default:
                //error
                throw new IllegalArgumentException("Неизвестный тип: " + decision);
        }
        return ordersAtCurrentTime.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public long addOrderFromImport(String description, Master master, GarageSpot garageSpot, LocalDateTime startTime, LocalDateTime endtime, BigDecimal price, User user) {
        Order order = new Order(description, master, garageSpot, startTime, endtime, price, user);
        orderRepository.save(order);
        return order.getId();
    }

    //сама записывает на ближайшее время
    @Transactional
    public OrderResponseDto addOrder(String description, int durationInHours, BigDecimal price, User inputUser) {
        User user = userRepository.findByUsername(inputUser.getUsername()).orElseThrow(
                () -> new NotFoundException("user with username " + inputUser.getUsername() + " not found")
        );
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bestStartTime = null;
        Master selectedMaster = null;
        GarageSpot selectedSpot = null;

        for (GarageSpot spot : this.getGarageSpotsWithCalendar()) {
            LocalDateTime candidateStart = spot.findNextAvailableSlotInGarageSpotSchedule(now, durationInHours);

            for (Master master : this.getMastersWithCalendar()) {
                if (master.isAvailable(candidateStart, candidateStart.plusHours(durationInHours))) {
                    if (bestStartTime == null || candidateStart.isBefore(bestStartTime)) {
                        bestStartTime = candidateStart;
                        selectedMaster = master;
                        selectedSpot = spot;
                    }

                }
            }
        }
        if (bestStartTime == null) {
            throw new RuntimeException("В системе отсувствует мастер или парковочное место");
        }
        LocalDateTime endTime = bestStartTime.plusHours(durationInHours);
        return this.addOrder(description, selectedMaster, selectedSpot, bestStartTime, endTime, price, user);
    }

    //запись на конкретное время (-1 - записаться не удалось)
    @Transactional
    public OrderResponseDto addOrderAtCurrentTime(LocalDateTime date, String description, int durationInHours, BigDecimal price, User inputUser) {
        User user = userRepository.findByUsername(inputUser.getUsername()).orElseThrow(
                () -> new NotFoundException("user with username " + inputUser.getUsername() + " not found")
        );
        for (var garageSpot : this.getGarageSpotsWithCalendar()) {
            if (garageSpot.isAvailable(date, date.plusHours(durationInHours))) {
                for (var master : this.getMastersWithCalendar()) {
                    if (master.isAvailable(date, date.plusHours(durationInHours))) {
                        return this.addOrder(description, master, garageSpot, date, date.plusHours(durationInHours), price, user);
                    }
                }
            }
        }
        throw new RuntimeException("В системе отсувствует мастер или парковочное место");
    }

    @Transactional
    public OrderResponseDto addOrderWithCurrentMaster(String description, int durationInHours, BigDecimal price, Long masterId, User inputUser) {
        User user = userRepository.findByUsername(inputUser.getUsername()).orElseThrow(
                () -> new NotFoundException("user with username " + inputUser.getUsername() + " not found")
        );
        Master master = masterRepository.findById(masterId).orElseThrow(
                () -> new NotFoundException("Master with id=" + masterId + " not found")
        );
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bestStartTime = null;
        GarageSpot selectedSpot = null;

        for (GarageSpot spot : this.getGarageSpotsWithCalendar()) {
            LocalDateTime candidateStart = spot.findNextAvailableSlotInGarageSpotSchedule(now, durationInHours);

            if (master.isAvailable(candidateStart, candidateStart.plusHours(durationInHours))) {
                if (bestStartTime == null || candidateStart.isBefore(bestStartTime)) {
                    bestStartTime = candidateStart;
                    selectedSpot = spot;
                }
            }
        }
        if (bestStartTime == null) {
            throw new RuntimeException("В системе отсувствует мастер или парковочное место");
        }
        LocalDateTime endTime = bestStartTime.plusHours(durationInHours);
        return this.addOrder(description, master, selectedSpot, bestStartTime, endTime, price, user);
    }

    @Transactional
    public void update(Order order) {
        orderRepository.update(order);
    }

    @Transactional
    public void deleteOrder(long id) {
        if (propertyUtil.isOrderAllowToDelete()) {
            orderRepository.delete(id);
        } else {
            throw new PermissionException("It is not allowed to delete orders due to application.properties!");
        }
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        List<Order> orders = orderRepository.findAll();
        for (var order : orders) {
            if (order.getEndTime().isBefore(LocalDateTime.now())) {
                order.setOrderStatus(OrderStatus.CLOSED);
            }
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order with id=" + id + " not found")
        );
        if (order.getEndTime().isBefore(LocalDateTime.now())) {
            order.setOrderStatus(OrderStatus.CLOSED);
        }
        return mapper.toDto(order);
    }

    public Order getOrderByIdImport(long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        } else {
            if (order.getEndTime().isBefore(LocalDateTime.now())) {
                order.setOrderStatus(OrderStatus.CLOSED);
            }
        }
        return order;
    }



    @Transactional
    public void closeOrder(long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order with id=" + id + " not found")
        );
        if (order.getOrderStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Заказ уже закрыт");
        }
        order.setOrderStatus(OrderStatus.CLOSED);
    }

    @Transactional
    public void cancelOrder(long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order with id=" + id + " not found")
        );
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Заказ уже отменен");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
    }

    //метод не используется
    @Transactional(readOnly = true)
    public long findOrderByTimeByCurrentMaster(Master master, LocalDateTime date) {
        List<Order> orders = orderRepository.findAll();
        for (var v : orders) {
            if (v.getMaster().equals(master) && v.getStartTime().isEqual(date)) {
                return v.getId();
            }
        }
        return -1;
    }

    @Transactional
    public void shiftOrder(long id, int durationToShiftInHours) {
        if (propertyUtil.isOrderAllowToShiftTime()) {
            List<Order> orders = getOrders();
            Order conflictSource = null;
            int startIndex = -1;

            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getId() == id) {
                    conflictSource = orders.get(i);
                    startIndex = i;
                    break;
                }
            }
            if (conflictSource == null) {
                throw new NotFoundException("Order with id=" + id + " not found");
            }
            conflictSource.setEndTime(
                    conflictSource.getEndTime().plusHours(durationToShiftInHours)
            );
            for (int i = startIndex + 1; i < orders.size(); i++) {

                Order candidate = orders.get(i);

                if (!candidate.getStartTime().isBefore(conflictSource.getEndTime())) {
                    continue;
                }

                boolean sameMaster =
                        candidate.getMaster().getId() ==
                                conflictSource.getMaster().getId();

                boolean sameGarageSpot =
                        candidate.getGarageSpot().getId() ==
                                conflictSource.getGarageSpot().getId();

                if (!sameMaster && !sameGarageSpot) {
                    continue;
                }

                Duration shift =
                        Duration.between(
                                candidate.getStartTime(),
                                conflictSource.getEndTime()
                        );

                candidate.setStartTime(
                        candidate.getStartTime().plus(shift)
                );
                candidate.setEndTime(
                        candidate.getEndTime().plus(shift)
                );

                conflictSource = candidate;
            }
        } else {
            throw new PermissionException("It is not allowed to shift orders due to application.properties!");
        }

    }

    @Transactional(readOnly = true)
    public Long getOrdersCount() {
        return orderRepository.count();
    }

    private OrderResponseDto addOrder(String description, Master master, GarageSpot garageSpot, LocalDateTime startTime, LocalDateTime endtime, BigDecimal price, User user) {
        Order order = new Order(description, master, garageSpot, startTime, endtime, price, user);
        orderRepository.save(order);
        return mapper.toDto(order);
    }


    private List<GarageSpot> getGarageSpotsWithCalendar() {
        List<GarageSpot> spots = garageSpotRepository.findAll();
        List<Object[]> slots = orderRepository.findTimeSlotsForAllGarageSpots();

        return GarageSpotDomainService.getGarageSpotsWithCalendar(spots, slots);
    }

    private List<Master> getMastersWithCalendar() {
        List<Master> masters = masterRepository.findAll();
        List<Object[]> slots = orderRepository.findTimeSlotsForAllMasters();

        return MasterDomainService.getMastersWithCalendar(masters, slots);
    }
}