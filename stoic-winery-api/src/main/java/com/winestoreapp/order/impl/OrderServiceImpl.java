package com.winestoreapp.order.impl;

import com.winestoreapp.exception.EntityNotFoundException;
import com.winestoreapp.exception.RegistrationException;
import com.winestoreapp.order.OrderService;
import com.winestoreapp.order.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.dto.CreateOrderDto;
import com.winestoreapp.order.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.dto.CreateShoppingCardDto;
import com.winestoreapp.order.dto.OrderDto;
import com.winestoreapp.order.mapper.OrderDeliveryInformationMapper;
import com.winestoreapp.order.mapper.OrderMapper;
import com.winestoreapp.order.model.Order;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import com.winestoreapp.order.model.PurchaseObject;
import com.winestoreapp.order.model.ShoppingCard;
import com.winestoreapp.order.repository.OrderDeliveryInformationRepository;
import com.winestoreapp.order.repository.OrderRepository;
import com.winestoreapp.order.repository.ShoppingCardRepository;
import com.winestoreapp.telegram.NotificationService;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.UserRepository;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final int USER_FIRST_NAME_INDEX = 0;
    private static final int USER_LAST_NAME_INDEX = 1;
    private static final String REGULAR_EXPRESSION_SPACES = "\\s+";
    private static final String SPACE = " ";
    private static final int WORD_QUANTITY = 2;

    private final Optional<NotificationService> notificationService;
    private final ShoppingCardRepository shoppingCardRepository;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;
    private final OrderDeliveryInformationMapper orderDeliveryInformationMapper;
    private final OrderDeliveryInformationRepository orderDeliveryInformationRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Value("${telegram.bot.enabled:false}")
    private boolean telegramBotEnable;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderDto dto) {
        String[] nameParts = validateAndParseName(dto.getUserFirstAndLastName());
        validateWinesExist(dto.getCreateShoppingCardDto());

        User user = findOrUpdateOrSaveUser(nameParts[USER_FIRST_NAME_INDEX],
                nameParts[USER_LAST_NAME_INDEX],
                dto.getPhoneNumber(), dto.getEmail());

        Order order = new Order();
        order.initializeNewOrder(user);

        order = orderRepository.save(order);
        order.generateAndSetOrderNumber();

        OrderDeliveryInformation delivery = createOrderDeliveryInformation(dto.getCreateOrderDeliveryInformationDto(), order);
        ShoppingCard card = createShoppingCard(dto.getCreateShoppingCardDto(), order);

        order.setDeliveryInformation(delivery);
        order.setShoppingCard(card);

        orderRepository.save(order);

        sendNotification(order, " is created.");
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public boolean updateOrderPaymentStatusAsPaidAndAddCurrentData(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find order by id: " + orderId));

        order.markAsPaid();
        orderRepository.save(order);

        sendNotification(order, " has been paid");
        return true;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find Order by id: " + id));

        orderRepository.deleteById(id);
        sendNotification(order, " has been deleted.");
        return true;
    }

    @Override
    public OrderDto getById(Long id) {
        return orderRepository.findById(id).map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Can't find Order by id: " + id));
    }

    @Override
    public List<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).stream().map(orderMapper::toDto).toList();
    }

    @Override
    public List<OrderDto> findAllByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        return orderRepository.findAllByUserId(userId, pageable).map(orderMapper::toDto).toList();
    }

    private ShoppingCard createShoppingCard(CreateShoppingCardDto dto, Order order) {
        ShoppingCard shoppingCard = new ShoppingCard();
        shoppingCard.setOrder(order);

        for (CreatePurchaseObjectDto objectDto : dto.getPurchaseObjects()) {
            Wine wine = wineRepository.findById(objectDto.getWineId())
                    .orElseThrow(() -> new EntityNotFoundException("Wine not found: " + objectDto.getWineId()));

            PurchaseObject po = new PurchaseObject();
            po.setWine(wine);
            po.setPrice(wine.getPrice());
            po.setQuantity(objectDto.getQuantity());

            shoppingCard.addPurchaseObject(po);
        }
        return shoppingCardRepository.save(shoppingCard);
    }

    private OrderDeliveryInformation createOrderDeliveryInformation(CreateOrderDeliveryInformationDto dto, Order order) {
        OrderDeliveryInformation info = orderDeliveryInformationMapper.toEntity(dto);
        info.linkToOrder(order);
        return orderDeliveryInformationRepository.save(info);
    }

    private String[] validateAndParseName(String fullName) {
        String[] parts = fullName.strip().replaceAll(REGULAR_EXPRESSION_SPACES, SPACE).split(SPACE);
        if (parts.length != WORD_QUANTITY) {
            throw new RegistrationException("You should enter your first and last name with a space between them");
        }
        return parts;
    }

    private void validateWinesExist(CreateShoppingCardDto cardDto) {
        for (CreatePurchaseObjectDto item : cardDto.getPurchaseObjects()) {
            if (!wineRepository.existsById(item.getWineId())) {
                throw new EntityNotFoundException("Can't find wine by id " + item.getWineId());
            }
        }
    }

    private void sendNotification(Order order, String actionMessage) {
        if (telegramBotEnable && order.getUser().getTelegramChatId() != null) {
            notificationService.ifPresent(service -> service.sendNotification(
                    "Your order: " + order.getOrderNumber() + actionMessage,
                    order.getUser().getTelegramChatId()
            ));
        }
    }

    private User findOrUpdateOrSaveUser(String fName, String lName, String phone, String email) {
        return userRepository.findUserByEmail(email)
                .or(() -> userRepository.findFirstByFirstNameAndLastName(fName, lName))
                .map(user -> {
                    user.setPhoneNumber(phone);
                    user.setEmail(email);
                    return userRepository.save(user);
                })
                .orElseGet(() -> userRepository.save(new User(email, fName, lName, phone)));
    }
}
