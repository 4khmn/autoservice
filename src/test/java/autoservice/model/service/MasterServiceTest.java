package autoservice.model.service;

import autoservice.model.dto.create.MasterCreateDto;
import autoservice.model.dto.response.MasterResponseDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.Master;
import autoservice.model.entities.Order;
import autoservice.model.entities.TimeSlot;
import autoservice.model.enums.MastersSortEnum;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.exceptions.PermissionException;
import autoservice.model.mapper.MasterMapper;
import autoservice.model.repository.MasterRepository;
import autoservice.model.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.postgresql.hostchooser.HostRequirement.master;

@ExtendWith(MockitoExtension.class)
public class MasterServiceTest {

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MasterMapper mapper;

    @InjectMocks
    private MasterService masterService;


    @Test
    void getMasterById_ShouldReturnsMasterResponseDto_WhenMasterExists() {
        Master master = new Master("denis", new BigDecimal(120.00));
        master.setId(1L);
        TimeSlot timeSlot = new TimeSlot(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        TreeSet<TimeSlot> slots = new TreeSet<>(Set.of(timeSlot));
        master.setCalendar(slots);
        MasterResponseDto responseDto = new MasterResponseDto(1L, "denis",  new BigDecimal(120.00));

        given(masterRepository.findById(1L)).willReturn(Optional.of(master));
        given(orderRepository.findTimeSlotsByMaster(1L)).willReturn(slots);
        given(mapper.toDto(master)).willReturn(responseDto);

        MasterResponseDto result = masterService.getMasterById(1L);

        assertEquals(responseDto, result);
        verify(mapper).toDto(master);
        verify(masterRepository).findById(1L);
        verify(orderRepository).findTimeSlotsByMaster(1L);

    }

    @Test
    void getMasterById_ShouldThrowNotFoundException_WhenMasterDoesNotExist() {
        given(masterRepository.findById(1L)).willReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> masterService.getMasterById(1L));

        assertEquals("Master with id: 1 not found", notFoundException.getMessage());
        verifyNoInteractions(mapper);
        verifyNoInteractions(orderRepository);
    }


    @Test
    void deleteMaster_ShouldDeleteMaster() {
        Long id = 1L;

        masterService.deleteMaster(id);

        verify(masterRepository).delete(id);
    }

    @Test
    void addMaster_ShouldReturnMasterResponseDt() {
        MasterCreateDto master = new MasterCreateDto("denis", new BigDecimal(120.00));
        MasterResponseDto responseDto = new MasterResponseDto(1L, "denis", new BigDecimal(120.00));

        given(mapper.toDto(any(Master.class))).willReturn(responseDto);

        MasterResponseDto result = masterService.addMaster(master);

        assertEquals(responseDto, result);
    }



    @Test
    void getMasterByIdImport_ShouldReturnMaster_WhenMasterExists() {
        Long id = 1L;
        Master master = new Master("denis", new BigDecimal(120.00));
        master.setId(id);
        TimeSlot timeSlot = new TimeSlot(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        TreeSet<TimeSlot> slots = new TreeSet<>(Set.of(timeSlot));
        master.setCalendar(slots);

        given(masterRepository.findById(id)).willReturn(Optional.of(master));
        given(orderRepository.findTimeSlotsByMaster(id)).willReturn(slots);

        Master result = masterService.getMasterByIdImport(id);

        assertEquals(master, result);
        verify(masterRepository).findById(id);
        verify(orderRepository).findTimeSlotsByMaster(id);
    }

    @Test
    void getMasterByIdImport_ShouldReturnNull_WhenMasterDoesNotExist() {
        given(masterRepository.findById(1L)).willReturn(Optional.empty());

        Master result = masterService.getMasterByIdImport(1);

        assertNull(result);
        verify(masterRepository).findById(1L);
        verify(orderRepository, never()).findTimeSlotsByMaster(1L);
    }

    @Test
    void getMasters_ShouldReturnListOfMasters_WhenMastersExist() {
        Master m1 = new Master("denis", new BigDecimal(120.00));
        m1.setId(1L);
        Master m2 = new Master("denis", new BigDecimal(120.00));
        m2.setId(2L);
        List<Master> mockSpots = List.of(m1, m2);

        LocalDateTime now = LocalDateTime.now();
        Object[] slotForSpot1 = new Object[] { 1L, now.plusHours(1), now.plusHours(2) };
        Object[] slotForSpot2 = new Object[] { 2L, now.plusHours(3), now.plusHours(4) };
        List<Object[]> mockSlots = List.of(slotForSpot1, slotForSpot2);

        given(masterRepository.findAll()).willReturn(mockSpots);
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(mockSlots);

        List<Master> result = masterService.getMasters();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertFalse(result.get(0).getCalendar().isEmpty());
        assertFalse(result.get(1).getCalendar().isEmpty());

        verify(masterRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
    }

    @Test
    void getMasters_ShouldReturnEmptyList_WhenMastersDoNotExist() {

        List<Master> mockSpots = List.of();
        List<Object[]> mockSlots = List.of();

        given(masterRepository.findAll()).willReturn(mockSpots);
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(mockSlots);

        List<Master> result = masterService.getMasters();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(masterRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllMasters();
    }


    @Test
    void getMasterByOrder_ShouldReturnMasterResponseDto_WhenOrderExists() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        Master master = new Master("denis", new BigDecimal(120.00));
        master.setId(orderId);
        order.setMaster(master);

        MasterResponseDto responseDto = new MasterResponseDto(1L, "denis", new BigDecimal(120.00));
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(mapper.toDto(master)).willReturn(responseDto);

        MasterResponseDto result = masterService.getMasterByOrder(orderId);
        assertEquals(responseDto, result);
        verify(orderRepository).findById(orderId);
        verify(mapper).toDto(master);
    }

    @Test
    void getMasterByOrder_ShouldThrowNotFoundException_WhenOrderDoesNotExist() {

        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> masterService.getMasterByOrder(1L));

        assertEquals("Order with id=1 not found", notFoundException.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void mastersSort_ShouldReturnListOfMasters_WhenSortByName() {
        Master m1 = new Master("aaa", new BigDecimal(120.00));
        Master m2 = new Master("bbb", new BigDecimal(120.00));

        MasterResponseDto mr1 = new MasterResponseDto(1L, "aaa", new BigDecimal(120.00));
        MasterResponseDto mr2 = new MasterResponseDto(2L, "bbb", new BigDecimal(120.00));
        given(masterRepository.mastersSortByName()).willReturn(List.of(m1, m2));
        given(mapper.toDto(m1)).willReturn(mr1);
        given(mapper.toDto(m2)).willReturn(mr2);

        List<MasterResponseDto> result = masterService.mastersSort(MastersSortEnum.BY_NAME);

        assertEquals(2, result.size());
        assertEquals(mr1, result.get(0));
        assertEquals(mr2, result.get(1));

        verify(masterRepository).mastersSortByName();
        verify(mapper, times(2)).toDto(any(Master.class));
    }

    @Test
    void mastersSort_ShouldReturnListOfMasters_WhenSortByEmployment() {
        Master m1 = new Master("aaa", new BigDecimal(120.00));
        Master m2 = new Master("bbb", new BigDecimal(120.00));
        m1.setId(1L);
        m2.setId(2L);
        List<Master> mockSpots = List.of(m1, m2);
        MasterResponseDto mr1 = new MasterResponseDto(1L, "aaa", new BigDecimal(120.00));
        MasterResponseDto mr2 = new MasterResponseDto(2L, "bbb", new BigDecimal(120.00));

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> master1Slots = new ArrayList<>();
        master1Slots.add(new Object[]{ 1L, now.minusHours(10), now.plusHours(10) });
        List<Object[]> master2Spots = new ArrayList<>();
        master2Spots.add(new Object[]{ 2L, now.minusHours(1), now.plusHours(1) });
        List<Object[]> mockSlots = List.of(master1Slots.get(0), master2Spots.get(0));

        given(masterRepository.findAll()).willReturn(mockSpots);
        given(orderRepository.findTimeSlotsForAllMasters()).willReturn(mockSlots);
        given(mapper.toDto(m1)).willReturn(mr1);
        given(mapper.toDto(m2)).willReturn(mr2);

        List<MasterResponseDto> result = masterService.mastersSort(MastersSortEnum.BY_EMPLOYMENT);

        assertEquals(2, result.size());
        assertEquals(mr2, result.get(0));
        assertEquals(mr1, result.get(1));

        verify(masterRepository, never()).mastersSortByName();
        verify(mapper, times(2)).toDto(any(Master.class));
    }

    @Test
    void mastersSort_ShouldThrowIllegalArgumentException_WhenDecisionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            masterService.mastersSort(null);
        });
    }
}
