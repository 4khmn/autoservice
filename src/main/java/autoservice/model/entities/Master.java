package autoservice.model.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
@Entity
@Table(name = "master")
public class Master implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "salary")
    private BigDecimal salary;

    @Transient
    private transient TreeSet<TimeSlot> calendar;

    public Master() {
        this.calendar = new TreeSet<>();
    }


    public void setCalendar(TreeSet<TimeSlot> calendar) {
        this.calendar = calendar;
    }

    public Master(String name, BigDecimal salary) {
        this.name = name;
        this.salary = salary;
        this.calendar = new TreeSet<>();
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id - " + id + ", name - " + name + ", salary - " + salary;
    }

    public boolean addBusyTime(LocalDateTime start, LocalDateTime end) {
        TimeSlot newSlot = new TimeSlot(start, end);
        calendar.add(newSlot);
        return true;
    }

    public void freeTimeSlot(LocalDateTime start, LocalDateTime end) {
        calendar.remove(new TimeSlot(start, end));
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        for (TimeSlot slot : calendar) {
            if (slot.overlaps(start, end)) {
                return false;
            }
        }
        return true;
    }


    public TreeSet<TimeSlot> getCalendar() {
        if (calendar == null) calendar = new TreeSet<>();
        return calendar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public LocalDateTime findNextAvailableSlotInMasterSchedule(LocalDateTime from, int durationInHours) {
        TreeSet<TimeSlot> calendar;

        calendar = getCalendar();

        if (calendar.isEmpty()) {
            return from;
        }
        TimeSlot firstSlot = calendar.first();
        if (from.isBefore(firstSlot.getStart().minusHours(durationInHours)) ||
        from.isEqual(firstSlot.getStart().minusHours(durationInHours))) {
            return from;
        }

        for (TimeSlot slot : calendar) {
            if (slot.getEnd().isBefore(from) || slot.getEnd().isEqual(from)) {
                continue;
            }
            TimeSlot next = calendar.higher(slot);
            LocalDateTime candidateStart = slot.getEnd();
            LocalDateTime candidateEnd = candidateStart.plusHours(durationInHours);

            if (next == null || candidateEnd.isBefore(next.getStart())) {
                if (candidateStart.isAfter(from)) {
                    return candidateStart;
                } else {
                    return from;
                }
            }
        }
        if (calendar.last().getEnd().isAfter(from)) {
            return calendar.last().getEnd();
        } else {
            return from;
        }
    }

}
