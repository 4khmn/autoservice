package autoservice.model.entities;

import autoservice.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order implements Comparable<Order>, Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    private Master master;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_spot_id")
    private GarageSpot garageSpot;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        if (orderStatus == null) {
            orderStatus = OrderStatus.OPEN;
        }
    }


    public Order() {
    }

    @JsonProperty("masterId")
    public void setMasterId(Long masterId) {
        if (masterId != null) {
            this.master = new Master();
            this.master.setId(masterId);
        } else {
            this.master = null;
        }
    }

    @JsonProperty("garageSpotId")
    public void setGarageSpotId(Long garageSpotId) {
        if (garageSpotId != null) {
            this.garageSpot = new GarageSpot();
            this.garageSpot.setId(garageSpotId);
        } else {
            this.garageSpot = null;
        }
    }

    @JsonProperty("masterId")
    public Long getMasterId() {
        return master != null ? master.getId() : null;
    }

    @JsonProperty("garageSpotId")
    public Long getGarageSpotId() {
        return garageSpot != null ? garageSpot.getId() : null;
    }




    public void setId(long id) {
        this.id = id;
    }



    public long getId() {
        return id;
    }


    public Order(String description,
                 Master master,
                 GarageSpot garageSpot,
                 LocalDateTime startTime,
                 LocalDateTime endTime,
                 BigDecimal price,
                 User user) {
        if (startTime.isBefore(endTime)) {
            this.description = description;
            this.master = master;
            this.garageSpot = garageSpot;
            this.startTime = startTime;
            this.endTime = endTime;
            this.price = price;
            this.createdAt = LocalDateTime.now();
            this.user = user;
        } else {
            throw new IllegalArgumentException("Invalid time settings: start time must be before end time");
        }
    }





    @Override
    public String toString() {
        return "id - " + id + ", description - " + description + ", created at - " + createdAt +
                ", master id - " + master.getId() + ", garage spot id - " + garageSpot.getId() + ", start time - " + startTime +
                ", end time - " + endTime + ", status - " + orderStatus.name().toLowerCase() + ", price - " + price;
    }

    @Override
    public int compareTo(Order other) {
        int cmp = this.startTime.compareTo(other.startTime);
        if (cmp == 0) {
            cmp = this.endTime.compareTo(other.endTime);
        }
        if (cmp == 0) {
            cmp = Long.compare(this.id, other.id); // или hashCode(), если нет id
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
