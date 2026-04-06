package autoservice.model.entities;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.TreeSet;
@Entity
@Table(name = "garage_spot")
public class GarageSpot implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size")
    private double size;

    @Column(name = "has_lift")
    private boolean hasLift;

    @Column(name = "has_pit")
    private boolean hasPit;

    @Transient
    private transient TreeSet<TimeSlot> calendar;


    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCalendar(TreeSet<TimeSlot> calendar) {
        this.calendar = calendar;
    }


    public GarageSpot() {
        this.calendar = new TreeSet<>();
    }

    public GarageSpot(double size, boolean hasLift, boolean hasPit) {

        this.size = size;
        this.hasLift = hasLift;
        this.hasPit = hasPit;
        this.calendar = new TreeSet<>();
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public boolean isHasLift() {
        return hasLift;
    }

    public void setHasLift(boolean hasLift) {
        this.hasLift = hasLift;
    }

    public boolean isHasPit() {
        return hasPit;
    }

    public void setHasPit(boolean hasPit) {
        this.hasPit = hasPit;
    }

    public boolean addBusyTime(LocalDateTime start, LocalDateTime end) {
        TimeSlot newSlot = new TimeSlot(start, end);
        calendar.add(newSlot);
        return true;
    }

    @Override
    public String toString() {
        return "id - " + id + ", size - " + size + ", hasList - " + hasLift + ", hasPit - " + hasPit;
    }

    public void freeAllSchedule() {
        this.calendar = new TreeSet<>();
    }
    public void freeTimeSlot(LocalDateTime start, LocalDateTime end) {
        calendar.remove(new TimeSlot(start, end));
    }

    public LocalDateTime getNextAvailableTime(int duration) {
        LocalDateTime currentTime = LocalDateTime.now();
        for (var v: calendar) {
            if (v.getStart().isAfter(currentTime)) {
                if (currentTime.plusHours(duration).isBefore(v.getStart())) {
                    return currentTime;
                }
            }
            currentTime = v.getEnd();
        }
        return currentTime;
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

    public boolean scheduleIsEmpty() {
        return calendar.isEmpty();
    }

    public LocalDateTime findNextAvailableSlotInGarageSpotSchedule(LocalDateTime from, int durationInHours) {
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
