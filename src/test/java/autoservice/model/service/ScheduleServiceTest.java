package autoservice.model.service;

import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.Master;
import autoservice.model.repository.GarageSpotRepository;
import autoservice.model.repository.MasterRepository;
import autoservice.model.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private GarageSpotRepository garageSpotRepository;
    @Mock
    private MasterRepository masterRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private final LocalDateTime testDate = LocalDateTime.of(2026, 5, 20, 10, 0);

    @Test
    void getNumberOfFreeSpots_ShouldReturnMastersCount_WhenMastersAreFewer() {
        GarageSpot g1 = new GarageSpot();
        g1.setId(1L);
        GarageSpot g2 = new GarageSpot();
        g2.setId(2L);
        given(garageSpotRepository.findAll()).willReturn(List.of(g1, g2));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());

        Master m = new Master();
        m.setId(1L);
        given(masterRepository.findAll()).willReturn(List.of(m));
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        Long result = scheduleService.getNumberOfFreeSpotsByDate(testDate);

        assertEquals(1L, result);

        verify(masterRepository).findAll();
        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void getNumberOfFreeSpots_ShouldReturnSpotsCount_WhenSpotsAreFewer() {

        GarageSpot s = new GarageSpot();
        s.setId(1L);
        given(garageSpotRepository.findAll()).willReturn(List.of(s));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());

        Master m1 = new Master();
        m1.setId(1L);
        Master m2 = new Master();
        m2.setId(2L);

        given(masterRepository.findAll()).willReturn(List.of(m1, m2));
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        Long result = scheduleService.getNumberOfFreeSpotsByDate(testDate);

        assertEquals(1L, result);

        verify(masterRepository).findAll();
        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void getNumberOfFreeSpots_ShouldReturnZero_WhenEntitiesExistButBusy() {

        Master master = new Master();
        master.setId(1L);
        given(masterRepository.findAll()).willReturn(List.of(master));

        List<Object[]> masterSlots = new ArrayList<>();
        masterSlots.add(new Object[]{ 1L, testDate.minusHours(3), testDate.plusHours(3) });
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(masterSlots);

        GarageSpot spot = new GarageSpot();
        spot.setId(1L);
        given(garageSpotRepository.findAll()).willReturn(List.of(spot));

        List<Object[]> spotSlots = new ArrayList<>();
        spotSlots.add(new Object[]{ 1L, testDate.minusHours(3), testDate.plusHours(3) });
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(spotSlots);

        Long result = scheduleService.getNumberOfFreeSpotsByDate(testDate);

        assertEquals(0, result);

        verify(masterRepository).findAll();
        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void getNumberOfFreeSpots_ShouldReturnZero_WhenDatabaseIsEmpty() {
        given(garageSpotRepository.findAll()).willReturn(List.of());
        given(masterRepository.findAll()).willReturn(List.of());
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        Long result = scheduleService.getNumberOfFreeSpotsByDate(testDate);

        assertEquals(0L, result);

        verify(masterRepository).findAll();
        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void getClosestDate_ShouldReturnNow_WhenAllAvailable() {
        GarageSpot spot = new GarageSpot();
        spot.setId(1L);
        Master master = new Master();
        master.setId(10L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of(master));
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        LocalDateTime result = scheduleService.getClosestDate(2);

        assertNotNull(result);
        assertTrue(result.isAfter(LocalDateTime.now().minusMinutes(1)));
        assertTrue(result.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void getClosestDate_ShouldReturnEarliestAvailableSlot() {
        LocalDateTime now = LocalDateTime.now();
        GarageSpot spot1 = new GarageSpot();
        spot1.setId(1L);
        GarageSpot spot2 = new GarageSpot();
        spot2.setId(2L);
        Master master = new Master();
        master.setId(10L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot1, spot2));
        given(masterRepository.findAll()).willReturn(List.of(master));

        List<Object[]> spot1Slots = new ArrayList<>();
        spot1Slots.add(new Object[]{ 1L, now.minusHours(1), now.plusHours(5) });

        List<Object[]> spot2Slots = new ArrayList<>();
        spot2Slots.add(new Object[]{ 2L, now.minusHours(1), now.plusHours(2) });

        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(List.of(spot1Slots.get(0), spot2Slots.get(0)));
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(new ArrayList<>());

        LocalDateTime result = scheduleService.getClosestDate(1);

        assertTrue(result.isAfter(now.plusHours(1).plusMinutes(59)));
        assertTrue(result.isBefore(now.plusHours(2).plusMinutes(1)));
    }

    @Test
    void getClosestDate_ShouldThrowException_WhenNoSlotsFound() {

        GarageSpot spot = new GarageSpot();
        spot.setId(1L);
        Master master = new Master();
        master.setId(10L);

        given(garageSpotRepository.findAll()).willReturn(List.of(spot));
        given(masterRepository.findAll()).willReturn(List.of(master));

        List<Object[]> masterSlots = new ArrayList<>();
        masterSlots.add(new Object[]{ 10L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(1) });
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(masterSlots);
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(new ArrayList<>());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                scheduleService.getClosestDate(1)
        );

        assertEquals("No available time slot found", exception.getMessage());
    }
}