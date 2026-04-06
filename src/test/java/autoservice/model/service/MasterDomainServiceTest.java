package autoservice.model.service;

import autoservice.model.entities.Master;
import autoservice.model.service.domain.MasterDomainService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MasterDomainServiceTest {

    @Test
    void getMastersWithCalendar_success() {
        Master master = new Master();
        master.setId(10L);
        List<Master> masters = List.of(master);

        List<Object[]> slots = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        slots.add(new Object[]{10L, start, LocalDateTime.now().plusHours(1)});

        List<Master> result = MasterDomainService.getMastersWithCalendar(masters, slots);

        assertEquals(1, result.get(0).getCalendar().size());
        assertEquals(start, result.get(0).getCalendar().first().getStart());
    }
}
