package com.winestoreapp.order.impl;

import com.winestoreapp.common.config.CustomMySqlContainer;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.exception.RegistrationException;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.order.api.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.api.dto.CreateShoppingCardDto;
import com.winestoreapp.order.api.dto.OrderDto;
import com.winestoreapp.order.api.dto.OrderPaymentStatus;
import com.winestoreapp.order.config.TestServiceConfig;
import com.winestoreapp.order.mapper.OrderDeliveryInformationMapper;
import com.winestoreapp.order.mapper.OrderMapper;
import com.winestoreapp.order.model.Order;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import com.winestoreapp.order.model.ShoppingCard;
import com.winestoreapp.order.repository.OrderRepository;
import com.winestoreapp.order.repository.ShoppingCardRepository;
import com.winestoreapp.telegram.api.NotificationService;
import com.winestoreapp.user.api.UserService;
import com.winestoreapp.user.api.dto.UserResponseDto;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineDto;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(classes = TestServiceConfig.class)
@ContextConfiguration(initializers = OrderServiceImplTest.Initializer.class)
@Transactional
class OrderServiceImplTest {
    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private WineService wineService;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderDeliveryInformationMapper orderDeliveryInformationMapper;

    @MockBean
    private ShoppingCardRepository shoppingCardRepository;

    @Test
    @DisplayName("Create order - should return OrderDto when data is valid")
    void createOrder_ValidData_ShouldReturnOrderDto() {
        CreateOrderDto createOrderDto = createOrderDto();

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(1L);

        Mockito.when(userService.getOrUpdateOrCreateUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userDto);

        WineDto wineDto = new WineDto();
        wineDto.setId(1L);
        wineDto.setName("Test Wine");
        wineDto.setPrice(BigDecimal.valueOf(870));

        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(wineService.findById(anyLong())).thenReturn(wineDto);
        Mockito.when(orderDeliveryInformationMapper.toEntity(any())).thenReturn(new OrderDeliveryInformation());
        Mockito.when(shoppingCardRepository.save(any())).thenReturn(new ShoppingCard());

        OrderDto expectedDto = new OrderDto();
        expectedDto.setOrderNumber("ORDER_123");
        Mockito.when(orderMapper.toDto(any(Order.class))).thenReturn(expectedDto);

        OrderDto result = orderService.createOrder(createOrderDto);

        assertNotNull(result);
        assertTrue(result.getOrderNumber().startsWith("ORDER_"));
    }

    @Test
    @DisplayName("Create order - should throw RegistrationException when name is invalid")
    void createOrder_InvalidFullName_ShouldThrowRegistrationException() {
        CreateOrderDto dto = createOrderDto();
        dto.setUserFirstAndLastName("Ivan"); // Only one word

        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> orderService.createOrder(dto)
        );

        assertEquals("You should enter your first and last name with a space between them", exception.getMessage());
    }

    @Test
    @DisplayName("Create order - should throw EntityNotFoundException when wine does not exist")
    void createOrder_NonExistentWine_ShouldThrowEntityNotFoundException() {
        CreateOrderDto dto = createOrderDto();

        Mockito.when(wineService.existsById(anyLong())).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.createOrder(dto)
        );

        assertTrue(exception.getMessage().contains("Can't find wine by id"));
    }

    @Test
    @DisplayName("Create order - should send Telegram notification when bot is enabled")
    void createOrder_ShouldSendTelegramNotification() {
        CreateOrderDto dto = createOrderDto();

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(1L);
        userDto.setTelegramChatId(12345L);

        Mockito.when(userService.getOrUpdateOrCreateUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userDto);

        WineDto wineDto = new WineDto();
        wineDto.setId(1L);
        wineDto.setName("Test Wine");
        wineDto.setPrice(BigDecimal.valueOf(100));
        Mockito.when(wineService.existsById(anyLong())).thenReturn(true);
        Mockito.when(wineService.findById(anyLong())).thenReturn(wineDto);

        Mockito.when(orderDeliveryInformationMapper.toEntity(any())).thenReturn(new OrderDeliveryInformation());
        Mockito.when(shoppingCardRepository.save(any())).thenReturn(new ShoppingCard());

        OrderDto expectedDto = new OrderDto();
        expectedDto.setOrderNumber("ORDER_123");
        Mockito.when(orderMapper.toDto(any(Order.class))).thenReturn(expectedDto);

        ReflectionTestUtils.setField(orderService, "telegramBotEnable", true);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ReflectionTestUtils.setField(orderService, "notificationService", Optional.of(notificationService));

        orderService.createOrder(dto);

        Mockito.verify(notificationService, Mockito.times(1))
                .sendNotification(Mockito.contains("Your order"), Mockito.eq(12345L));
    }

    @Test
    @DisplayName("Mark as paid - should update status to PAID")
    void markAsPaid_ValidId_ShouldUpdateStatus() {
        Order order = new Order();
        order.initializeNewOrder(1L);
        order = orderRepository.save(order);
        order.generateAndSetOrderNumber();
        order = orderRepository.save(order);

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(1L);
        Mockito.when(userService.loadUserById(anyLong())).thenReturn(userDto);

        boolean result = orderService.markAsPaid(order.getId());

        assertTrue(result);
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderPaymentStatus.PAID, updated.getPaymentStatus());
        assertNotNull(updated.getCompletedTime());
    }

    @Test
    @DisplayName("Mark as paid - should throw EntityNotFoundException for non-existent order")
    void markAsPaid_NonExistentOrder_ShouldThrowEntityNotFoundException() {
        Mockito.when(userService.loadUserById(anyLong())).thenReturn(new UserResponseDto());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.markAsPaid(999L)
        );

        assertTrue(exception.getMessage().contains("Can't find order by id"));
    }

    @Test
    @DisplayName("Delete order - should throw EntityNotFoundException for non-existent order")
    void deleteById_NonExistentOrder_ShouldThrowEntityNotFoundException() {
        Mockito.when(userService.loadUserById(anyLong())).thenReturn(new UserResponseDto());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.deleteById(999L)
        );

        assertTrue(exception.getMessage().contains("Can't find Order by id"));
    }

    private CreateOrderDto createOrderDto() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setUserFirstAndLastName("Ivan Ivanov");
        dto.setEmail("ivan@test.com");
        dto.setPhoneNumber("+380000000");

        CreatePurchaseObjectDto po = new CreatePurchaseObjectDto();
        po.setWineId(1L);
        po.setQuantity(1);

        CreateShoppingCardDto card = new CreateShoppingCardDto();
        card.setPurchaseObjects(Set.of(po));

        dto.setCreateShoppingCardDto(card);
        dto.setCreateOrderDeliveryInformationDto(new CreateOrderDeliveryInformationDto());
        return dto;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            CustomMySqlContainer container = CustomMySqlContainer.getInstance();
            container.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + container.getJdbcUrl(),
                    "spring.datasource.username=" + container.getUsername(),
                    "spring.datasource.password=" + container.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=update",
                    "telegram.bot.enabled=false"
            ).applyTo(context.getEnvironment());
        }
    }
}
