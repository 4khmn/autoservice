package autoservice.model.repository;

import autoservice.model.entities.Order;
import autoservice.model.entities.TimeSlot;
import autoservice.model.enums.OrderSortField;
import autoservice.model.enums.OrderStatus;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@Repository
public class OrderRepository extends HibernateAbstractDAO<Order, Long> {

    public OrderRepository() {
        super(Order.class);
    }

    @Override
    public List<Order> findAll() {
        String hql = "FROM Order o ORDER BY o.startTime";
        return getSession().createQuery(hql, Order.class).getResultList();
    }


    public List<Object[]> findTimeSlotsForAllGarageSpots() {
        String hql = """
            SELECT o.garageSpot.id, o.startTime, o.endTime
            FROM Order o
            WHERE o.orderStatus <> :cancelled
            """;
        return getSession().createQuery(hql, Object[].class)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
    }

    public TreeSet<TimeSlot> findTimeSlotsByGarageSpot(long garageSpotId) {
        String hql = """
            SELECT new autoservice.model.entities.TimeSlot(o.startTime, o.endTime)
            FROM Order o
            WHERE o.garageSpot.id = :garageSpotId
            AND o.orderStatus <> :cancelled
            """;
        List<TimeSlot> result = getSession().createQuery(hql, TimeSlot.class)
                .setParameter("garageSpotId", garageSpotId)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
        return new TreeSet<>(result);
    }

    public List<Object[]> findTimeSlotsForAllMasters() {
        String hql = """
            SELECT o.master.id, o.startTime, o.endTime
            FROM Order o
            WHERE o.orderStatus <> :cancelled
            """;
        return getSession().createQuery(hql, Object[].class)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
    }

    public TreeSet<TimeSlot> findTimeSlotsByMaster(long masterId) {
        String hql = """
            SELECT new autoservice.model.entities.TimeSlot(o.startTime, o.endTime)
            FROM Order o
            WHERE o.master.id = :masterId
            AND o.orderStatus <> :cancelled
            """;
        List<TimeSlot> result = getSession().createQuery(hql, TimeSlot.class)
                .setParameter("masterId", masterId)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
        return new TreeSet<>(result);
    }

    public Optional<Order> getOrderByMaster(long masterId) {
        String hql = "FROM Order o WHERE o.master.id = :masterId";
        return getSession().createQuery(hql, Order.class)
                .setParameter("masterId", masterId)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    public List<Order> ordersSortByPrice(boolean onlyActive) {
        return getOrdersSorted(OrderSortField.PRICE, onlyActive);
    }

    public List<Order> ordersSortByEndDate(boolean onlyActive) {
        return getOrdersSorted(OrderSortField.END_TIME, onlyActive);
    }

    public List<Order> ordersSortByCreationDate(boolean onlyActive) {
        return getOrdersSorted(OrderSortField.CREATED_AT, onlyActive);
    }

    public List<Order> ordersSortByStartDate() {
        return getOrdersSorted(OrderSortField.START_TIME, false);
    }

    public List<Order> getOrdersSorted(OrderSortField sortField, boolean onlyActive) {
        StringBuilder hql = new StringBuilder("FROM Order o WHERE o.orderStatus NOT IN (:closedStatuses) ");
        if (onlyActive) {
            hql.append("AND :now BETWEEN o.startTime AND o.endTime ");
        }
        hql.append("ORDER BY o.").append(sortField.getFieldName());

        Query<Order> query = getSession().createQuery(hql.toString(), Order.class)
                .setParameter("closedStatuses", List.of(OrderStatus.CANCELLED, OrderStatus.CLOSED));

        if (onlyActive) {
            query.setParameter("now", LocalDateTime.now());
        }
        return query.getResultList();
    }


    public List<Order> ordersSortByTimeFrameByCreationDate(LocalDateTime start, LocalDateTime end) {
        return getOrdersByTimeFrameSorted(start, end, OrderSortField.CREATED_AT);
    }

    public List<Order> ordersSortByTimeFrameByEndDate(LocalDateTime start, LocalDateTime end) {
        return getOrdersByTimeFrameSorted(start, end, OrderSortField.END_TIME);
    }

    public List<Order> ordersSortByTimeFrameByPrice(LocalDateTime start, LocalDateTime end) {
        return getOrdersByTimeFrameSorted(start, end, OrderSortField.PRICE);
    }

    public List<Order> getOrdersByTimeFrameSorted(LocalDateTime start, LocalDateTime end, OrderSortField sortField) {
        String hql = "FROM Order o WHERE o.startTime <= :start AND o.endTime >= :end ORDER BY o." + sortField.getFieldName();
        return getSession().createQuery(hql, Order.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}