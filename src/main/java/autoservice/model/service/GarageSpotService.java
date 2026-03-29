package autoservice.model.service;

import autoservice.model.dto.create.GarageSpotCreateDto;
import autoservice.model.dto.response.GarageSpotResponseDto;
import autoservice.model.entities.GarageSpot;
import autoservice.model.exceptions.IllegalGarageSpotSize;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.exceptions.PermissionException;
import autoservice.model.mapper.GarageSpotMapper;
import autoservice.model.repository.GarageSpotRepository;
import autoservice.model.repository.OrderRepository;
import autoservice.model.service.domain.GarageSpotDomainService;
import autoservice.model.utils.PropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class GarageSpotService {

    private transient final GarageSpotRepository garageSpotRepository;
    private transient final OrderRepository orderRepository;
    private transient final GarageSpotMapper mapper;
    private final PropertyUtil propertyUtil;


    @Transactional
    public GarageSpotResponseDto addGarageSpot(GarageSpotCreateDto garageSpotCreateDto) {
        if (propertyUtil.isGarageSpotAllowToAddRemove()) {
            if (garageSpotCreateDto.size() < 8) {
                throw new IllegalGarageSpotSize("garage-spot size has to be more than 7");
            }
            GarageSpot garageSpot = new GarageSpot(garageSpotCreateDto.size(), garageSpotCreateDto.hasLift(), garageSpotCreateDto.hasPit());
            garageSpotRepository.save(garageSpot);
            return mapper.toDto(garageSpot);
        } else {
            throw new PermissionException("It is not allowed to add garage-spots due to application.properties!");
        }
    }

    @Transactional
    public void deleteGarageSpot(long id) {
        if (propertyUtil.isGarageSpotAllowToAddRemove()) {
            garageSpotRepository.delete(id);
        } else {
            throw new PermissionException("It is not allowed to delete garage-spots due to application.properties!");
        }
    }

    @Transactional
    public void update(GarageSpot garageSpot) {
        garageSpotRepository.update(garageSpot);
    }

    @Transactional(readOnly = true)
    public List<GarageSpot> getGarageSpots() {
        List<GarageSpot> spots = garageSpotRepository.findAll();

        List<Object[]> slots = orderRepository.findTimeSlotsForAllGarageSpots();

        return GarageSpotDomainService.getGarageSpotsWithCalendar(spots, slots);
    }

    @Transactional(readOnly = true)
    public Long getGarageSpotsCount() {
        return garageSpotRepository.count();
    }

    //4
    @Transactional(readOnly = true)
    public GarageSpotResponseDto getGarageSpotById(long id) {
        GarageSpot spot = garageSpotRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Garage spot with id=" + id + " not found")
        );
        spot.setCalendar(
                orderRepository.findTimeSlotsByGarageSpot(id)
        );
        return mapper.toDto(spot);
    }

    public GarageSpot getGarageSpotByIdImport(long id) {
        GarageSpot garageSpot = garageSpotRepository.findById(id).orElse(null);
        if (garageSpot == null) {
            return null;
        } else {
            garageSpot.setCalendar(
                    orderRepository.findTimeSlotsByGarageSpot(id)
            );
        }
        return garageSpot;
    }

    //4 список свободных мест в сервисных гаражах
    @Transactional(readOnly = true)
    public List<GarageSpotResponseDto> getFreeSpots() {
        List<GarageSpot> freeGarageSpots = new ArrayList<>();
        List<GarageSpot> garageSpots = getGarageSpots();
        for (var v : garageSpots) {
            if (v.isAvailable(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1))) {
                freeGarageSpots.add(v);
            }
        }
        return freeGarageSpots.stream().map(mapper::toDto).toList();
    }
}
