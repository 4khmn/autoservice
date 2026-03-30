package autoservice.model.service;

import autoservice.model.entities.GarageSpot;
import autoservice.model.service.domain.GarageSpotDomainService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GarageSpotDomainServiceTest {

    @Test
    void getGarageSpotsWithCalendar_success() {
        GarageSpot spot = new GarageSpot();
        spot.setId(1L);
        List<GarageSpot> spots = List.of(spot);

        List<Object[]> slots = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        slots.add(new Object[]{1L, start, LocalDateTime.now().plusHours(1)});

        List<GarageSpot> result = GarageSpotDomainService.getGarageSpotsWithCalendar(spots, slots);

        assertFalse(result.get(0).getCalendar().isEmpty());
        assertEquals(start, result.get(0).getCalendar().first().getStart());
    }
}
