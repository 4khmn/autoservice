package autoservice.model.controller;

import autoservice.model.dto.create.OrderCreateDto;
import autoservice.model.dto.response.MasterResponseDto;
import autoservice.model.dto.response.OrderResponseDto;
import autoservice.model.entities.User;
import autoservice.model.enums.ActiveOrdersSortEnum;
import autoservice.model.enums.OrdersSortByTimeFrameEnum;
import autoservice.model.enums.OrdersSortEnum;
import autoservice.model.service.MasterService;
import autoservice.model.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final MasterService masterService;
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable() long id) {
        log.info("GET /api/orders/{} - fetching order by id={}", id, id);
        OrderResponseDto order = orderService.getOrderById(id);
        log.info("order with id={} was successfully fetched", id);
        return ResponseEntity.ok(order);
    }


    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderCreateDto dto, @AuthenticationPrincipal User user) {
        log.info("POST /api/orders - creating order={}", dto);
        OrderResponseDto order;
        if (dto.date() == null) {
            if (dto.masterId() == null) {
                order = orderService.addOrder(dto.description(), dto.durationInHours(), dto.price(), user);
            } else {
                order = orderService.addOrderWithCurrentMaster(dto.description(), dto.durationInHours(), dto.price(), dto.masterId(), user);
            }
        } else {
            order = orderService.addOrderAtCurrentTime(
                    dto.date(), dto.description(), dto.durationInHours(), dto.price(), user
            );
        }
        log.info("order with id={} was successfully created", order.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable long id) {
        log.info("DELETE /api/orders - deleting order={}", id);
        orderService.deleteOrder(id);
        log.info("order with id={} was successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/master")
    public ResponseEntity<MasterResponseDto> getMasterByOrderId(@PathVariable("orderId") long orderId) {
        log.info("Getting master by order with id{}", orderId);
        MasterResponseDto masterByOrder = masterService.getMasterByOrder(orderId);
        log.info("Master successfully found by order with master_id={}", masterByOrder.id());
        return ResponseEntity.ok(masterByOrder);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeOrder(@PathVariable("id") long id) {
        log.info("PATCH /api/orders/{}/close - closing order with id={}", id, id);
        orderService.closeOrder(id);
        log.info("Order with id={} successfully closed", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable("id") long id) {
        log.info("PATCH /api/orders/{}/cancel - canceling order with id={}", id, id);
        orderService.cancelOrder(id);
        log.info("Order with id={} successfully cancelled", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/shift")
    public ResponseEntity<Void> shiftOrder(@PathVariable("id") long id,
                                           @RequestParam(required = true) int duration) {
        log.info("PATCH /api/orders/{}/shift - shifting order with id={}", id, id);
        orderService.shiftOrder(id, duration);
        log.info("Order with id={} successfully shifted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(
            @RequestParam(name = "sort", defaultValue = "BY_CREATION_DATE") OrdersSortEnum decision) {
        log.info("GET /api/orders?sort={} - sorting orders by decision={}", decision, decision);
        List<OrderResponseDto> orders = orderService.ordersSort(decision);
        log.info("Orders successfully sorted by decision={}", decision);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrderResponseDto>> getActiveOrders(
            @RequestParam(name = "sort", defaultValue = "BY_CREATION_DATE") ActiveOrdersSortEnum decision) {
        log.info("GET /api/orders/active?sort={} - sorting active orders by decision={}", decision, decision);
        List<OrderResponseDto> activeOrders = orderService.activeOrdersSort(decision);
        log.info("Active orders successfully sorted by decision={}", decision);
        return ResponseEntity.ok(activeOrders);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDto>> getOrdersHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "BY_CREATION_DATE") OrdersSortByTimeFrameEnum sort) {

        log.info("GET /api/orders/history - sorting orders by time frame [{} - {}] by decision={}", start, end, sort);
        List<OrderResponseDto> orders = orderService.ordersSortByTimeFrame(start, end, sort);
        log.info("Orders successfully sorted by time frame [{} - {}] by decision={}", start, end, sort);
        return ResponseEntity.ok(orders);
    }
}
