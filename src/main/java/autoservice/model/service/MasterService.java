package autoservice.model.service;

import autoservice.model.dto.create.MasterCreateDto;
import autoservice.model.dto.response.MasterResponseDto;
import autoservice.model.entities.Master;
import autoservice.model.entities.Order;
import autoservice.model.enums.MastersSortEnum;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.mapper.MasterMapper;
import autoservice.model.repository.MasterRepository;
import autoservice.model.repository.OrderRepository;
import autoservice.model.service.domain.MasterDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;
@Service
@RequiredArgsConstructor
public class MasterService {
    private transient final MasterRepository masterRepository;
    private transient final OrderRepository orderRepository;
    private transient final MasterMapper mapper;


    //4 список авто-мастеров
    @Transactional(readOnly = true)
    public List<MasterResponseDto> mastersSort(MastersSortEnum decision) {
        List<Master> sortedMasters;
        if (decision == null) {
            throw new IllegalArgumentException("Decision cannot be null");
        }
        switch (decision) {
            case BY_NAME:
                //по алфавиту
                sortedMasters = masterRepository.mastersSortByName();
                break;
            case BY_EMPLOYMENT:
                //по занятости
                sortedMasters = getMasters().stream()
                        .sorted(Comparator.comparingInt(this::getFreeMasterHoursToday)
                                .reversed() //сначала те, у кого больше свободных часов
                                .thenComparing(Master::getName)) //затем по имени
                        .toList();
                break;
            default:
                //error
                throw new IllegalArgumentException("IInvalid decision: " + decision);
        }
        return sortedMasters.stream()
                .map(mapper::toDto)
                .toList();
    }

    //4 мастер, выполняющий конкретный заказ
    @Transactional(readOnly = true)
    public MasterResponseDto getMasterByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException("Order with id=" + orderId + " not found")
        );
        Master master = order.getMaster();
        return mapper.toDto(master);
    }

    @Transactional(readOnly = true)
    public List<Master> getMasters() {
        List<Master> masters = masterRepository.findAll();
        List<Object[]> slots = orderRepository.findTimeSlotsForAllMasters();

        return MasterDomainService.getMastersWithCalendar(masters, slots);
    }


    @Transactional
    public MasterResponseDto addMaster(MasterCreateDto masterCreateDto) {
        Master master = new Master(masterCreateDto.name(), masterCreateDto.salary());
        masterRepository.save(master);
        return mapper.toDto(master);
    }

    @Transactional
    public void deleteMaster(long id) {
        masterRepository.delete(id);
    }

    @Transactional
    public void update(Master master) {
        masterRepository.update(master);
    }

    @Transactional(readOnly = true)
    public MasterResponseDto getMasterById(long id) {
        Master master = masterRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Master with id: " + id + " not found")
        );
        master.setCalendar(
                orderRepository.findTimeSlotsByMaster(id)
        );
        return mapper.toDto(master);
    }

    public Master getMasterByIdImport(long id) {
        Master master = masterRepository.findById(id).orElse(null);
        if (master == null) {
            return null;
        } else {
            master.setCalendar(
                    orderRepository.findTimeSlotsByMaster(id)
            );
        }
        return master;
    }

    private int getFreeMasterHoursToday(Master master) {
        int freeMasterHoursToday = 24;
        int today = LocalDateTime.now().getDayOfMonth();
        for (var v : master.getCalendar()) {
            if (v.getStart().getDayOfMonth() == today && v.getEnd().getDayOfMonth() == today) {
                long duration = abs(Duration.between(v.getStart(), v.getEnd()).toHours());
                freeMasterHoursToday -= duration;
            } else if (v.getStart().getDayOfMonth() == today) {
                long duration = abs(Duration.between(v.getStart(),
                        LocalDateTime.of(v.getStart().getYear(),
                                v.getStart().getMonth(),
                                v.getStart().getDayOfMonth() + 1,
                                0,
                                0)).toHours());
                freeMasterHoursToday -= duration;
            } else if (v.getEnd().getDayOfMonth() == today) {
                long duration = abs(Duration.between(LocalDateTime.of(v.getEnd().getYear(),
                        v.getEnd().getMonth(),
                        v.getEnd().getDayOfMonth(),
                        0,
                        0), v.getEnd()).toHours());
                freeMasterHoursToday -= duration;
            }
        }
        return freeMasterHoursToday;
    }

    public Long getMastersCount() {
        return masterRepository.count();
    }
}