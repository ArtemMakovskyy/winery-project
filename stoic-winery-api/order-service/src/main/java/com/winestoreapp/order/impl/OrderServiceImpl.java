package com.winestoreapp.order.impl;

import com.winestoreapp.order.api.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.api.dto.CreateShoppingCardDto;
import com.winestoreapp.order.api.dto.OrderDto;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.order.api.OrderService;
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
import com.winestoreapp.wine.api.WineService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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

    @Value("${telegram.bot.enabled:false}")
    private boolean telegramBotEnable;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderDto dto) {
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

        OrderDeliveryInformation delivery = createOrderDeliveryInformation(
                dto.getCreateOrderDeliveryInformationDto(), order);
        ShoppingCard card = createShoppingCard(dto.getCreateShoppingCardDto(), order);

        order.setDeliveryInformation(delivery);
        order.setShoppingCard(card);

        orderRepository.save(order);

        sendNotification(order, " is created.", userDto);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public boolean markAsPaid(Long orderId) {
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
    public boolean deleteById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find Order by id: " + id));

        orderRepository.deleteById(id);

        UserResponseDto userDto = userService.loadUserById(order.getUserId());
        sendNotification(order, " has been deleted.", userDto);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
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
    public List<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAllByUserId(Long userId, Pageable pageable) {
        userService.loadUserById(userId);
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDto> findByOrderNumber(String orderNumber) {
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
            throw new RegistrationException("You should enter your first and last name "
                    + "with a space between them");
        }
        return parts;
    }

    private void validateWinesExist(CreateShoppingCardDto cardDto) {
        for (CreatePurchaseObjectDto item : cardDto.getPurchaseObjects()) {
            if (!wineService.existsById(item.getWineId())) {
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
}
