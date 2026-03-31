package autoservice.model.service;

import autoservice.model.dto.create.MasterCreateDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.Master;
import autoservice.model.entities.Order;
import autoservice.model.entities.User;
import autoservice.model.service.io.imports.CsvImportService;
import autoservice.model.utils.PropertyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CsvImportServiceTest {


    @Mock
    private OrderService orderService;

    @Mock
    private GarageSpotService garageSpotService;

    @Mock
    private MasterService masterService;

    @Mock
    private PropertyUtil propertyUtil;

    @Mock
    private UserService userService;


    @InjectMocks
    private CsvImportService csvImportService;

    @Test
    void importMasters_success() {
        // Данные в файле:
        // id,name,salary
        // 1,Ivan,500.00  (обновим)
        // 2,Oleg,600.00  (создадим)
        Master existingMaster = new Master();
        existingMaster.setId(1L);

        given(masterService.getMasterByIdImport(1L)).willReturn(existingMaster);
        given(masterService.getMasterByIdImport(2L)).willReturn(null);

        csvImportService.importMasters();

        assertEquals("Ivan", existingMaster.getName());
        assertEquals(new BigDecimal("500.00"), existingMaster.getSalary());

        verify(masterService).addMaster(new MasterCreateDto("Oleg", new BigDecimal("600.00")));
    }

    @Test
    void importGarageSpots_success() {
        // Данные в файле:
        // id,size,hasLift,hasPit
        // 1,20.0,true,true  (обновим)
        // 2,15.0,false,true (создадим)
        GarageSpot existingSpot = new GarageSpot();
        existingSpot.setId(1L);

        given(garageSpotService.getGarageSpotByIdImport(1L)).willReturn(existingSpot);
        given(garageSpotService.getGarageSpotByIdImport(2L)).willReturn(null);
        given(propertyUtil.isGarageSpotAllowToAddRemove()).willReturn(true);

        csvImportService.importGarageSpots();

        assertEquals(20.0, existingSpot.getSize());
        assertTrue(existingSpot.isHasLift());
        verify(garageSpotService).addGarageSpot(argThat(dto ->
                dto.size() == 15.0 && !dto.hasLift() && dto.hasPit()
        ));
    }

    @Test
    void importOrders_success() {
        // Данные в файле:
        // id,description,masterId,garageId,startTime,endTime,status,price,userId
        // 1,Repair,10,100,2026-03-30T10:00:00,2026-03-30T12:00:00,OPEN,150.0,5
        LocalDateTime start = LocalDateTime.parse("2026-03-30T10:00:00");
        LocalDateTime end = LocalDateTime.parse("2026-03-30T12:00:00");

        Master master = spy(new Master());
        master.setId(10L);
        GarageSpot spot = spy(new GarageSpot());
        spot.setId(100L);
        User user = new User();
        user.setId(5L);

        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setMaster(master);
        existingOrder.setGarageSpot(spot);
        existingOrder.setStartTime(start.minusDays(1));
        existingOrder.setEndTime(end.minusDays(1));

        given(orderService.getOrderByIdImport(1L)).willReturn(existingOrder);
        given(masterService.getMasterByIdImport(10L)).willReturn(master);
        given(garageSpotService.getGarageSpotByIdImport(100L)).willReturn(spot);
        given(userService.getUserById(5L)).willReturn(user);

        given(master.isAvailable(start, end)).willReturn(true);
        given(spot.isAvailable(start, end)).willReturn(true);

        csvImportService.importOrders();

        verify(master).freeTimeSlot(any(), any());
        verify(master).addBusyTime(start, end);

        assertEquals("Repair", existingOrder.getDescription());
        assertEquals(new BigDecimal("150.0"), existingOrder.getPrice());
        assertEquals(start, existingOrder.getStartTime());
    }

}
