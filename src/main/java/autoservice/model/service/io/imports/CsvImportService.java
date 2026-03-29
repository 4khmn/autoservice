package autoservice.model.service.io.imports;

import autoservice.model.dto.create.GarageSpotCreateDto;
import autoservice.model.dto.create.MasterCreateDto;
import autoservice.model.entities.User;
import autoservice.model.service.UserService;
import autoservice.model.utils.PropertyUtil;
import autoservice.model.entities.GarageSpot;
import autoservice.model.entities.Master;
import autoservice.model.entities.Order;
import autoservice.model.enums.OrderStatus;
import autoservice.model.exceptions.CsvParsingException;
import autoservice.model.exceptions.IllegalGarageSpotSize;
import autoservice.model.exceptions.ImportException;
import autoservice.model.service.GarageSpotService;
import autoservice.model.service.MasterService;
import autoservice.model.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Slf4j
@RequiredArgsConstructor
@Service
public class CsvImportService {
    private  final OrderService orderService;
    private final GarageSpotService garageSpotService;
    private final MasterService masterService;
    private final PropertyUtil propertyUtil;
    private final UserService userService;

    private final String garageSpotHeader = "id,size,hasLift,hasPit";
    private final String masterHeader = "id,name,salary";
    private final String orderHeader = "id,description,masterId,garageSpotId,startTime,endTime,orderStatus,price,userId";

    @Transactional
    public void importMasters() {
        try {
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("data/masters.csv")) {
                if (input == null) {
                    log.error("Import master file not found");
                    throw new ImportException("masters.csv не найден в resources");
                }


                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    String header = reader.readLine();
                    if (header == null) {
                        throw new ImportException("Файл пуст");
                    }
                    if (!header.equals(masterHeader)) {
                        log.error("Invalid header in master file");
                        throw new ImportException("Некорректный хедер в CSV: " + header);
                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        long id;
                        String name;
                        BigDecimal salary;
                        try {
                            String[] split = line.split(",");
                            if (split.length != 3) {
                                log.error("Invalid structure of master file line: {}", line);
                                throw new ImportException("Неверная структура строки: " + line);
                            }
                            id = Integer.parseInt(split[0]);
                            name = split[1];
                            salary = new BigDecimal(split[2]);
                        } catch (Exception e) {
                            log.error("Invalid master file line: {}", line);
                            throw new CsvParsingException("Ошибка в строке (line - " + line + ")");
                        }
                        Master master = masterService.getMasterByIdImport(id);
                        if  (master != null) {
                            master.setName(name);
                            master.setSalary(salary);
                        } else {
                            masterService.addMaster(new MasterCreateDto(name, salary));
                        }
                    }
                }
            }


        } catch (Exception e) {
            log.error("Import masters failed", e);
            throw new ImportException("Импорт не выполнен: " + e.getMessage());
        }
    }
    @Transactional
    public void importGarageSpots() {
        try {
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("data/garageSpots.csv")) {
                if (input == null) {
                    log.error("Import garage spot file not found");
                    throw new ImportException("garageSpots.csv не найден в resources");
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    String header = reader.readLine();
                    if (header == null) {
                        throw new ImportException("Файл пуст");
                    }
                    if (!header.equals(garageSpotHeader)) {
                        log.error("Invalid header in garage spot file");
                        throw new ImportException("Некорректный хедер в CSV: " + header);
                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        long id;
                        double size;
                        boolean hasLift;
                        boolean hasPit;
                        try {
                            String[] split = line.split(",");
                            if (split.length != 4) {
                                log.error("Invalid structure of garage spot file line: {}", line);
                                throw new ImportException("Неверная структура строки: " + line);
                            }
                            id = Long.parseLong(split[0]);
                            size = Double.parseDouble(split[1]);
                            hasLift = booleanParser(split[2]);
                            hasPit = booleanParser(split[3]);
                        } catch (Exception e) {
                            log.error("Invalid garage spot file line: {}", line);
                            throw new CsvParsingException("Ошибка в строке (line - " + line + ")\n" + e.getMessage());
                        }
                        if (size < 8) {
                            log.error("Invalid garage spot size in line: {}", line);
                            throw new IllegalGarageSpotSize("Минимальный размер гаража - 8 (line - " + line + ")");
                        } else {
                            GarageSpot garageSpot = garageSpotService.getGarageSpotByIdImport(id);
                            if (garageSpot != null) {
                                garageSpot.setSize(size);
                                garageSpot.setHasLift(hasLift);
                                garageSpot.setHasPit(hasPit);
                            } else {
                                if (propertyUtil.isGarageSpotAllowToAddRemove()) {
                                    garageSpotService.addGarageSpot(new GarageSpotCreateDto(size, hasLift, hasPit));
                                } else {
                                    log.error("Immpossible to add due properties file permission");
                                    throw new CsvParsingException("невозможно добавить из за properties (line - " + line + ")");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Import garage spots failed", e);
            throw new ImportException("Импорт не выполнен: " + e.getMessage());

        }
    }

    @Transactional
    public void importOrders() {
        try {
            try (InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("data/orders.csv")) {
                if (input == null) {
                    log.error("Import orders file not found");
                    throw new ImportException("orders.csv не найден в resources");
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    String header = reader.readLine();
                    if (header == null) {
                        throw new ImportException("Файл пуст");
                    }
                    if (!header.equals(orderHeader)) {
                        log.error("Invalid header in orders file");
                        throw new ImportException("Некорректный хедер в CSV: " + header);
                    }
                    String line;

                    while ((line = reader.readLine()) != null) {
                        long id;
                        String description;
                        long masterId;
                        long garageId;
                        LocalDateTime startTime;
                        LocalDateTime endTime;
                        OrderStatus orderStatus;
                        BigDecimal price;
                        Long userId;
                        try {
                            String[] split = line.split(",");
                            if (split.length != 9) {
                                log.error("Invalid structure of orders file line: {}", line);
                                throw new ImportException("Неверная структура строки: " + line);
                            }
                            id = Long.parseLong(split[0]);
                            description = split[1];
                            masterId = Long.parseLong(split[2]);
                            garageId = Long.parseLong(split[3]);
                            startTime = LocalDateTime.parse(split[4]);
                            endTime = LocalDateTime.parse(split[5]);
                            orderStatus = OrderStatus.valueOf(split[6]);
                            price = new BigDecimal(split[7]);
                            userId = Long.parseLong(split[8]);
                        } catch (Exception e) {
                            log.error("Invalid orders file line: {}", line);
                            throw new CsvParsingException("Ошибка в строке (line - " + line + ")\n" + e.getMessage());
                        }
                        Order order = orderService.getOrderByIdImport(id);
                        //если такой ордер уже есть - меняем его поля
                        if (order != null) {
                            order.setOrderStatus(orderStatus);
                            order.setPrice(price);
                            order.setDescription(description);
                            User user = userService.getUserById(userId);
                            order.setUser(user);
                            Master master = masterService.getMasterByIdImport(masterId);
                            GarageSpot garageSpot = garageSpotService.getGarageSpotByIdImport(garageId);
                            if (master == null || garageSpot == null) {
                                //невалидный мастер или гараж
                                log.error("Invalid master or garage spot, line: {}", line);
                                throw new ImportException("Невалидный мастер или гараж (line - " + line + ")");
                            }
                            if (master.getId() == order.getMasterId()) {
                                master = order.getMaster();
                            }
                            if (garageSpot.getId() == order.getGarageSpotId()) {
                                garageSpot = order.getGarageSpot();
                            }
                                order.getMaster().freeTimeSlot(order.getStartTime(), order.getEndTime());
                                order.getGarageSpot().freeTimeSlot(order.getStartTime(), order.getEndTime());
                                if (master.isAvailable(startTime, endTime) && garageSpot.isAvailable(startTime, endTime)) {
                                    order.setGarageSpot(garageSpot);
                                    order.setMaster(master);
                                    order.setStartTime(startTime);
                                    order.setEndTime(endTime);
                                    master.addBusyTime(startTime, endTime);
                                    garageSpot.addBusyTime(startTime, endTime);
                                } else {
                                    //невозможно добавить по новому мастеру или гаражу, возвращаем изначальное значение
                                    order.getMaster().addBusyTime(order.getStartTime(), order.getEndTime());
                                    order.getGarageSpot().addBusyTime(order.getStartTime(), order.getEndTime());
                                    log.error("Impossible to add due masters or garage spot schedule, line: {}", line);
                                    throw new ImportException("Нельзя изменить из-за расписания мастера или гаража (line - " + line + ")");
                                }
                        } else {
                            //новый заказ
                            Master master = masterService.getMasterByIdImport(masterId);
                            GarageSpot garageSpot = garageSpotService.getGarageSpotByIdImport(garageId);
                            User user = userService.getUserById(userId);
                            if (master != null && garageSpot != null) {
                                if (master.isAvailable(startTime, endTime) && garageSpot.isAvailable(startTime, endTime)) {
                                    orderService.addOrderFromImport(description, master, garageSpot, startTime, endTime, price, user);
                                } else {
                                    //нельзя из за расписания мастера или гаража
                                    log.error("Impossible to add due master or garage spot schedule, line: {}", line);
                                    throw new ImportException("Нельзя добавить из-за расписания мастера или гаража (line - " + line + ")");
                                }
                            } else {
                                //такого мастера или гаража нету, невозможно создать связь
                                log.error("Impossible to add due master or garage spot existence , line: {}", line);
                                throw new ImportException("Такого мастера или гаража нету, невозможно создать связь (line - " + line + ")");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Import orders failed", e);
            throw new ImportException("Импорт не выполнен: " + e.getMessage());
        }
    }

    public boolean booleanParser(String value) {
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            throw new CsvParsingException("Illegal boolean value: " + value);
        }
    }
}