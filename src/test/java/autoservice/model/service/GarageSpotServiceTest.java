package autoservice.model.service;

import autoservice.model.dto.create.GarageSpotCreateDto;
import autoservice.model.dto.response.GarageSpotResponseDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.TimeSlot;
import autoservice.model.exceptions.IllegalGarageSpotSize;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.exceptions.PermissionException;
import autoservice.model.mapper.GarageSpotMapper;
import autoservice.model.repository.GarageSpotRepository;
import autoservice.model.repository.OrderRepository;
import autoservice.model.utils.PropertyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GarageSpotServiceTest {

    @Mock
    private GarageSpotRepository garageSpotRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GarageSpotMapper mapper;

    @Mock
    private PropertyUtil propertyUtil;

    @InjectMocks
    private GarageSpotService garageSpotService;


    @Test
    void getGarageSpotById_ShouldReturnGarageSpotResponseDto_WhenGarageSpotExists() {

        GarageSpot garageSpot = new GarageSpot(10, true, true);
        garageSpot.setId(1L);
        TimeSlot timeSlot = new TimeSlot(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        TreeSet<TimeSlot> slots = new TreeSet<>(Set.of(timeSlot));
        garageSpot.setCalendar(slots);

        GarageSpotResponseDto dto = new GarageSpotResponseDto(1L, 10, true, true);
        given(garageSpotRepository.findById(1L)).willReturn(Optional.of(garageSpot));
        given(orderRepository.findTimeSlotsByGarageSpot(1L)).willReturn(slots);
        given(mapper.toDto(garageSpot)).willReturn(dto);

        GarageSpotResponseDto result = garageSpotService.getGarageSpotById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(garageSpotRepository).findById(1L);
        verify(orderRepository).findTimeSlotsByGarageSpot(1L);
    }

    @Test
    void getGarageSpotById_ShouldThrowNotFoundException_WhenGarageSpotDoesNotExist() {

        Long id = 1L;
        given(garageSpotRepository.findById(id)).willReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> garageSpotService.getGarageSpotById(1L));

        assertEquals("Garage spot with id=" + id + " not found", notFoundException.getMessage());

        verifyNoInteractions(mapper);
        verifyNoInteractions(orderRepository);
    }


    @Test
    void getGarageSpots_ShouldReturnListOfGarageSpots_WhenGarageSpotsExist() {
        GarageSpot spot1 = new GarageSpot(10, true, true);
        spot1.setId(1L);
        GarageSpot spot2 = new GarageSpot(20, true, true);
        spot2.setId(2L);
        List<GarageSpot> mockSpots = List.of(spot1, spot2);

        LocalDateTime now = LocalDateTime.now();
        Object[] slotForSpot1 = new Object[] { 1L, now.plusHours(1), now.plusHours(2) };
        Object[] slotForSpot2 = new Object[] { 2L, now.plusHours(3), now.plusHours(4) };
        List<Object[]> mockSlots = List.of(slotForSpot1, slotForSpot2);

        given(garageSpotRepository.findAll()).willReturn(mockSpots);
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(mockSlots);

        List<GarageSpot> result = garageSpotService.getGarageSpots();

        assertNotNull(result);
        assertEquals(2, result.size());


        assertFalse(result.get(0).getCalendar().isEmpty());
        assertFalse(result.get(1).getCalendar().isEmpty());

        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void getGarageSpots_ShouldReturnEmptyList_WhenGarageSpotsDoNotExist() {

        List<GarageSpot> mockSpots = List.of();
        List<Object[]> mockSlots = List.of();

        given(garageSpotRepository.findAll()).willReturn(mockSpots);
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(mockSlots);

        List<GarageSpot> result = garageSpotService.getGarageSpots();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(garageSpotRepository).findAll();
        verify(orderRepository).findTimeSlotsForAllGarageSpots();
    }

    @Test
    void deleteGarageSpot_ShouldDeleteGarageSpot_WhenItIsAllowed() {
        Long id = 1L;

        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(true);

        garageSpotService.deleteGarageSpot(id);

        verify(garageSpotRepository).delete(id);
    }

    @Test
    void deleteGarageSpot_ShouldThrowPermissionException_WhenItIsNotAllowed() {
        Long id = 1L;

        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(false);

        assertThrows(PermissionException.class,
                () -> garageSpotService.deleteGarageSpot(id));

        verifyNoInteractions(garageSpotRepository);
    }



    @Test
    void addGarageSpot_ShouldReturnGarageSpotResponseDto_WhenSizeMoreThat8_WhenItIsAllowed() {
        GarageSpotCreateDto garageSpotCreateDto = new GarageSpotCreateDto(10, true, true);
        GarageSpotResponseDto garageSpotResponseDto = new GarageSpotResponseDto(1L, 10, true, true);
        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(true);
        given(mapper.toDto(any(GarageSpot.class))).willReturn(garageSpotResponseDto);

        GarageSpotResponseDto result = garageSpotService.addGarageSpot(garageSpotCreateDto);

        assertNotNull(result);
        assertEquals(garageSpotResponseDto, result);

        verify(mapper).toDto(any(GarageSpot.class));
        verify(garageSpotRepository).save(any(GarageSpot.class));
    }

    @Test
    void addGarageSpot_ShouldThrowIllegalGarageSpotSize_WhenSizeMoreThat8_WhenItIsNotAllowed() {
        GarageSpotCreateDto garageSpotCreateDto = new GarageSpotCreateDto(10, true, true);

        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(false);

        assertThrows(PermissionException.class,
                () -> garageSpotService.addGarageSpot(garageSpotCreateDto));
        verifyNoInteractions(garageSpotRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void addGarageSpot_ShouldThrowIllegalGarageSpotSize_WhenSizeLessThan8_WhenItIsAllowed() {
        GarageSpotCreateDto garageSpotCreateDto = new GarageSpotCreateDto(6, true, true);

        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(true);

        assertThrows(IllegalGarageSpotSize.class,
                () -> garageSpotService.addGarageSpot(garageSpotCreateDto));

        verifyNoInteractions(garageSpotRepository);
        verifyNoInteractions(mapper);

    }

    @Test
    void addGarageSpot_ShouldThrowPermissionException_WhenSizeLessThat8_WhenItIsNotAllowed() {
        GarageSpotCreateDto garageSpotCreateDto = new GarageSpotCreateDto(6, true, true);

        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(false);

        assertThrows(PermissionException.class,
                () -> garageSpotService.addGarageSpot(garageSpotCreateDto));
        verifyNoInteractions(garageSpotRepository);
        verifyNoInteractions(mapper);

    }

    @Test
    void getGarageSpotByIdImport_ShouldReturnGarageSpot_WhenGarageSpotExists() {
        Long id = 1L;
        GarageSpot garageSpot = new GarageSpot(10, true, true);

        TimeSlot timeSlot = new TimeSlot(LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        TreeSet<TimeSlot> slots = new TreeSet<>(Set.of(timeSlot));
        garageSpot.setCalendar(slots);

        given(garageSpotRepository.findById(id)).willReturn(Optional.of(garageSpot));
        given(orderRepository.findTimeSlotsByGarageSpot(id)).willReturn(slots);

        GarageSpot result = garageSpotService.getGarageSpotByIdImport(id);

        assertNotNull(result);
        assertEquals(garageSpot, result);

        verify(garageSpotRepository).findById(id);
        verify(orderRepository).findTimeSlotsByGarageSpot(id);
    }

    @Test
    void getGarageSpotByIdImport_ShouldReturnNull_WhenGarageSpotDoesNotExist() {
        given(garageSpotRepository.findById(1L)).willReturn(Optional.empty());
        GarageSpot result = garageSpotService.getGarageSpotByIdImport(1);

        assertNull(result);
        verify(garageSpotRepository).findById(1L);
        verify(orderRepository, never()).findTimeSlotsByGarageSpot(1L);
    }

    @Test
    void getFreeSpots_ShouldReturnListOfGarageSpotResponseDtos_WhenGarageSpotsExist() {

        LocalDateTime now = LocalDateTime.now();

        GarageSpot freeSpot = new GarageSpot(10, true, true);
        freeSpot.setId(1L);
        GarageSpot busySpot = new GarageSpot(10, true, true);
        busySpot.setId(2L);
        List<GarageSpot> spots = List.of(freeSpot, busySpot);

        Object[] busyRow = new Object[] { 2L, now.minusHours(1), now.plusHours(1) };
        List<Object[]> slotsData = new ArrayList<>();
        slotsData.add(busyRow);

        given(garageSpotRepository.findAll()).willReturn(spots);
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(slotsData);

        GarageSpotResponseDto dto = new GarageSpotResponseDto(1L, 10, true, true);
        given(mapper.toDto(any(GarageSpot.class))).willReturn(dto);

        List<GarageSpotResponseDto> result = garageSpotService.getFreeSpots();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }


    @Test
    void getFreeSpots_ShouldReturnEmptyList_WhenGarageSpotsDoNotExist() {

        LocalDateTime now = LocalDateTime.now();


        GarageSpot busySpot = new GarageSpot(10, true, true);
        busySpot.setId(1L);
        List<GarageSpot> spots = List.of(busySpot);

        Object[] busyRow = new Object[] { 1L, now.minusHours(1), now.plusHours(1) };
        List<Object[]> slotsData = new ArrayList<>();
        slotsData.add(busyRow);

        given(garageSpotRepository.findAll()).willReturn(spots);
        given(orderRepository.findTimeSlotsForAllGarageSpots()).willReturn(slotsData);

        List<GarageSpotResponseDto> result = garageSpotService.getFreeSpots();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(mapper, never()).toDto(any(GarageSpot.class));
    }

}
