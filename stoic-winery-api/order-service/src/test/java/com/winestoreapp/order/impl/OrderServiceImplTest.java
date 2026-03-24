package com.winestoreapp.order.impl;

import com.winestoreapp.common.config.AbstractMySQLContainerTest;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.order.api.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.api.dto.CreateShoppingCardDto;
import com.winestoreapp.order.api.dto.OrderDto;
import com.winestoreapp.order.api.dto.OrderPaymentStatus;
import com.winestoreapp.order.api.dto.ShoppingCardDto;
import com.winestoreapp.order.config.TestServiceConfig;
import com.winestoreapp.order.mapper.OrderDeliveryInformationMapper;
import com.winestoreapp.order.mapper.OrderMapper;
import com.winestoreapp.order.model.Order;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import com.winestoreapp.order.repository.OrderDeliveryInformationRepository;
import com.winestoreapp.order.repository.OrderRepository;
import com.winestoreapp.order.repository.ShoppingCardRepository;
import com.winestoreapp.telegram.api.NotificationService;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(classes = TestServiceConfig.class)
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "telegram.bot.enabled=false"
})
class OrderServiceImplTest extends AbstractMySQLContainerTest {

    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDeliveryInformationRepository deliveryRepository;
    @Autowired
    private ShoppingCardRepository shoppingCardRepository;

    @MockBean
    private UserService userService;
    @MockBean
    private WineService wineService;
    @MockBean
    private OrderMapper orderMapper;
    @MockBean
    private OrderDeliveryInformationMapper orderDeliveryInformationMapper;

    @Test
    void createOrder_Valid() {
        CreateOrderDto dto = createOrderDto();

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);

        Mockito.when(userService.getOrUpdateOrCreateUser(any(), any(), any(), any()))
                .thenReturn(user);

        WineDto wine = new WineDto();
        wine.setId(1L);
        wine.setPrice(BigDecimal.TEN);

        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(wineService.findById(anyLong())).thenReturn(wine);

        Mockito.when(orderDeliveryInformationMapper.toEntity(any()))
                .thenReturn(new OrderDeliveryInformation());

        Mockito.when(orderMapper.toDto(any()))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    OrderDto d = new OrderDto();
                    d.setOrderNumber(o.getOrderNumber());
                    return d;
                });

        OrderDto result = orderService.createOrder(dto);

        assertNotNull(result);
        assertNotNull(result.getOrderNumber());
    }

    @Test
    void createOrder_InvalidName() {
        CreateOrderDto dto = createOrderDto();
        dto.setUserFirstAndLastName("Ivan");

        assertThrows(RegistrationException.class,
                () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_WineNotFound() {
        CreateOrderDto dto = createOrderDto();

        Mockito.when(wineService.existsById(anyLong())).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_SendNotification() {
        CreateOrderDto dto = createOrderDto();

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);
        user.setTelegramChatId(111L);

        Mockito.when(userService.getOrUpdateOrCreateUser(any(), any(), any(), any()))
                .thenReturn(user);

        WineDto wine = new WineDto();
        wine.setId(1L);
        wine.setPrice(BigDecimal.TEN);
        wine.setName("Wine");

        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(wineService.findById(anyLong())).thenReturn(wine);

        Mockito.when(orderDeliveryInformationMapper.toEntity(any()))
                .thenReturn(new OrderDeliveryInformation());

        Mockito.when(orderMapper.toDto(any()))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    OrderDto d = new OrderDto();
                    d.setOrderNumber(o.getOrderNumber());
                    return d;
                });

        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ReflectionTestUtils.setField(orderService, "notificationService", Optional.of(notificationService));
        ReflectionTestUtils.setField(orderService, "telegramBotEnable", true);

        orderService.createOrder(dto);

        Mockito.verify(notificationService)
                .sendNotification(contains("is created"), eq(111L));
    }

    @Test
    void markAsPaid_Success() {
        Order order = new Order();
        order.setUserId(1L);
        order = orderRepository.save(order);

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);

        Mockito.when(userService.loadUserById(anyLong())).thenReturn(user);
        ReflectionTestUtils.setField(orderService, "telegramBotEnable", false);

        boolean result = orderService.markAsPaid(order.getId());

        assertTrue(result);

        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderPaymentStatus.PAID, updated.getPaymentStatus());
    }

    @Test
    void deleteById_Success() {
        Order order = new Order();
        order.setUserId(1L);
        order = orderRepository.save(order);

        UserResponseDto user = new UserResponseDto();
        user.setId(1L);

        Mockito.when(userService.loadUserById(anyLong())).thenReturn(user);
        ReflectionTestUtils.setField(orderService, "telegramBotEnable", false);

        boolean result = orderService.deleteById(order.getId());

        assertTrue(result);
        assertTrue(orderRepository.findById(order.getId()).isEmpty());
    }

    @Test
    void getById_Success() {
        Order order = new Order();
        order.setUserId(1L);
        order = orderRepository.save(order);

        WineDto wine = new WineDto();
        wine.setId(1L);
        wine.setName("Wine");
        wine.setPictureLink("link");

        Mockito.when(wineService.findById(anyLong())).thenReturn(wine);

        OrderDto dto = new OrderDto();
        ShoppingCardDto cardDto = new ShoppingCardDto();
        cardDto.setPurchaseObjects(Set.of());
        dto.setShoppingCardDto(cardDto);

        Mockito.when(orderMapper.toDto(any())).thenReturn(dto);

        OrderDto result = orderService.getById(order.getId());

        assertNotNull(result);
    }

    @Test
    void findAll_ReturnsList() {
        Order order = new Order();
        order.setUserId(1L);
        orderRepository.save(order);

        Mockito.when(orderMapper.toDto(any())).thenReturn(new OrderDto());

        List<OrderDto> result = orderService.findAll(PageRequest.of(0, 10));

        assertEquals(1, result.size());
    }

//    @Test
//    void findByOrderNumber_Success() {
//        Order order = new Order();
//        order.setUserId(1L);
//        order.setOrderNumber("ORD-123");
//        orderRepository.save(order);
//
//        Mockito.when(orderMapper.toDto(any())).thenReturn(new OrderDto());
//
//        Optional<OrderDto> result =
//                orderService.findByOrderNumber("ORD-123");
//
//        assertTrue(result.isPresent());
//    }

    private CreateOrderDto createOrderDto() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setUserFirstAndLastName("Ivan Ivanov");
        dto.setEmail("test@test.com");
        dto.setPhoneNumber("1234567890");

        CreatePurchaseObjectDto po = new CreatePurchaseObjectDto();
        po.setWineId(1L);
        po.setQuantity(1);

        CreateShoppingCardDto card = new CreateShoppingCardDto();
        card.setPurchaseObjects(Set.of(po));

        dto.setCreateShoppingCardDto(card);
        dto.setCreateOrderDeliveryInformationDto(
                new CreateOrderDeliveryInformationDto());

        return dto;
    }
}