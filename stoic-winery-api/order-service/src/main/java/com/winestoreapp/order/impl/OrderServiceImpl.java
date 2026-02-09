package com.winestoreapp.order.impl;

import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.order.api.OrderService;
import com.winestoreapp.order.api.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.api.dto.CreateShoppingCardDto;
import com.winestoreapp.order.api.dto.OrderDto;
import com.winestoreapp.order.mapper.OrderDeliveryInformationMapper;
import com.winestoreapp.order.mapper.OrderMapper;
import com.winestoreapp.order.model.Order;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import com.winestoreapp.order.model.PurchaseObject;
import com.winestoreapp.order.model.ShoppingCard;
import com.winestoreapp.order.repository.OrderDeliveryInformationRepository;
import com.winestoreapp.order.repository.OrderRepository;
import com.winestoreapp.order.repository.ShoppingCardRepository;
import com.winestoreapp.telegram.api.NotificationService;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static final int USER_FIRST_NAME_INDEX = 0;
    private static final int USER_LAST_NAME_INDEX = 1;
    private static final String REGULAR_EXPRESSION_SPACES = "\\s+";
    private static final String SPACE = " ";
    private static final int WORD_QUANTITY = 2;

    private final Optional<NotificationService> notificationService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderDeliveryInformationMapper orderDeliveryInformationMapper;
    private final WineService wineService;
    private final OrderMapper orderMapper;
    private final OrderDeliveryInformationRepository orderDeliveryInformationRepository;
    private final ShoppingCardRepository shoppingCardRepository;
    private final Tracer tracer;

    @Value("${telegram.bot.enabled:false}")
    private boolean telegramBotEnable;

    @Override
    @Transactional
    @Observed(name = "order.service", contextualName = "create-order")
    public OrderDto createOrder(CreateOrderDto dto) {
        log.info("Creating new order for email: {}", dto.getEmail());
        String[] nameParts = validateAndParseName(dto.getUserFirstAndLastName());
        validateWinesExist(dto.getCreateShoppingCardDto());

        UserResponseDto userDto = userService.getOrUpdateOrCreateUser(
                dto.getEmail(),
                nameParts[USER_FIRST_NAME_INDEX],
                nameParts[USER_LAST_NAME_INDEX],
                dto.getPhoneNumber()
        );

        Order order = new Order();
        order.initializeNewOrder(userDto.getId());

        order = orderRepository.save(order);
        order.generateAndSetOrderNumber();

        tagSpan("order.number", order.getOrderNumber());
        tagSpan("user.id", userDto.getId());

        OrderDeliveryInformation delivery = createOrderDeliveryInformation(
                dto.getCreateOrderDeliveryInformationDto(), order);
        ShoppingCard card = createShoppingCard(dto.getCreateShoppingCardDto(), order);

        order.setDeliveryInformation(delivery);
        order.setShoppingCard(card);

        orderRepository.save(order);
        log.info("Order created successfully with number: {}", order.getOrderNumber());

        sendNotification(order, " is created.", userDto);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    @Observed(name = "order.service", contextualName = "mark-as-paid")
    public boolean markAsPaid(Long orderId) {
        log.info("Marking order as paid, id: {}", orderId);
        tagSpan("order.id", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find order by id: " + orderId));

        order.markAsPaid();
        orderRepository.save(order);

        UserResponseDto userDto = userService.loadUserById(order.getUserId());
        sendNotification(order, " has been paid", userDto);
        return true;
    }

    @Override
    @Transactional
    @Observed(name = "order.service", contextualName = "delete-order")
    public boolean deleteById(Long id) {
        log.info("Deleting order with id: {}", id);
        tagSpan("order.id", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find Order by id: " + id));

        orderRepository.deleteById(id);

        UserResponseDto userDto = userService.loadUserById(order.getUserId());
        sendNotification(order, " has been deleted.", userDto);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "order.service", contextualName = "get-order-by-id")
    public OrderDto getById(Long id) {
        tagSpan("order.id", id);
        OrderDto orderDto = orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Can't find Order by id: " + id));

        if (orderDto.getShoppingCardDto() != null && orderDto.getShoppingCardDto().getPurchaseObjects() != null) {
            orderDto.getShoppingCardDto().getPurchaseObjects().forEach(item -> {
                WineDto wineDto = wineService.findById(item.getWineId());
                item.setWineName(wineDto.getName());
                item.setWinePictureLink(wineDto.getPictureLink());
            });
        }

        return orderDto;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "order.service", contextualName = "find-all-orders")
    public List<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "order.service", contextualName = "find-all-by-user-id")
    public List<OrderDto> findAllByUserId(Long userId, Pageable pageable) {
        tagSpan("user.id", userId);
        userService.loadUserById(userId);
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "order.service", contextualName = "find-by-order-number")
    public Optional<OrderDto> findByOrderNumber(String orderNumber) {
        tagSpan("order.number", orderNumber);
        return orderRepository.findOrderByOrderNumber(orderNumber)
                .map(orderMapper::toDto);
    }

    private ShoppingCard createShoppingCard(CreateShoppingCardDto dto, Order order) {
        ShoppingCard shoppingCard = new ShoppingCard();
        shoppingCard.setOrder(order);

        for (CreatePurchaseObjectDto objectDto : dto.getPurchaseObjects()) {
            WineDto wineDto = wineService.findById(objectDto.getWineId());

            PurchaseObject po = new PurchaseObject();
            po.setWineId(wineDto.getId());
            po.setPrice(wineDto.getPrice());
            po.setQuantity(objectDto.getQuantity());
            shoppingCard.addPurchaseObject(po);
        }
        return shoppingCardRepository.save(shoppingCard);
    }

    private OrderDeliveryInformation createOrderDeliveryInformation(
            CreateOrderDeliveryInformationDto dto, Order order) {
        OrderDeliveryInformation info = orderDeliveryInformationMapper.toEntity(dto);
        info.linkToOrder(order);
        return orderDeliveryInformationRepository.save(info);
    }

    private String[] validateAndParseName(String fullName) {
        String[] parts = fullName.strip().replaceAll(REGULAR_EXPRESSION_SPACES, SPACE).split(SPACE);
        if (parts.length != WORD_QUANTITY) {
            log.warn("Invalid name format provided: {}", fullName);
            throw new RegistrationException("You should enter your first and last name "
                    + "with a space between them");
        }
        return parts;
    }

    private void validateWinesExist(CreateShoppingCardDto cardDto) {
        for (CreatePurchaseObjectDto item : cardDto.getPurchaseObjects()) {
            if (!wineService.existsById(item.getWineId())) {
                log.error("Wine validation failed for id: {}", item.getWineId());
                throw new EntityNotFoundException("Can't find wine by id " + item.getWineId());
            }
        }
    }

    private void sendNotification(Order order, String actionMessage, UserResponseDto userDto) {
        if (telegramBotEnable && userDto.getTelegramChatId() != null) {
            notificationService.ifPresent(service -> service.sendNotification(
                    "Your order: " + order.getOrderNumber() + actionMessage,
                    userDto.getTelegramChatId()
            ));
        }
    }

    private void tagSpan(String key, Object value) {
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag(key, String.valueOf(value));
        }
    }
}