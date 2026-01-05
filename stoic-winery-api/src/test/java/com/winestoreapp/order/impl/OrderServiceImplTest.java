package com.winestoreapp.order.impl;

import com.winestoreapp.order.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.dto.CreateOrderDto;
import com.winestoreapp.order.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.dto.CreateShoppingCardDto;
import com.winestoreapp.order.dto.OrderDto;
import com.winestoreapp.order.mapper.OrderDeliveryInformationMapper;
import com.winestoreapp.order.mapper.OrderMapper;
import com.winestoreapp.order.model.Order;
import com.winestoreapp.order.model.OrderDeliveryInformation;
import com.winestoreapp.order.model.OrderPaymentStatus;
import com.winestoreapp.order.model.PurchaseObject;
import com.winestoreapp.order.model.ShoppingCard;
import com.winestoreapp.order.repository.OrderDeliveryInformationRepository;
import com.winestoreapp.order.repository.OrderRepository;
import com.winestoreapp.order.repository.PurchaseObjectRepository;
import com.winestoreapp.order.repository.ShoppingCardRepository;
import com.winestoreapp.user.model.User;
import com.winestoreapp.user.repository.UserRepository;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private WineRepository wineRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PurchaseObjectRepository purchaseObjectRepository;
    @Mock
    private OrderDeliveryInformationMapper orderDeliveryInformationMapper;
    @Mock
    private OrderDeliveryInformationRepository orderDeliveryInformationRepository;
    @Mock
    private ShoppingCardRepository shoppingCardRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Add order by valid data and return OrderDto")
    void createOrder_ValidData_ShouldReturnOrderDto() {
        // given
        CreateOrderDto createOrderDto = getCreateOrderDto();
        User user = getUser(createOrderDto);
        OrderDto expectedOrderDto = getOrderDto(createOrderDto);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                ReflectionTestUtils.setField(o, "id", 1L);
            }
            return o;
        });

        when(wineRepository.existsById(anyLong())).thenReturn(true);
        when(wineRepository.findById(anyLong())).thenReturn(Optional.of(getWine()));
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findFirstByFirstNameAndLastName(anyString(), anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(orderDeliveryInformationMapper.toEntity(any())).thenReturn(new OrderDeliveryInformation());
        when(orderDeliveryInformationRepository.save(any())).thenReturn(new OrderDeliveryInformation());
        when(shoppingCardRepository.save(any())).thenReturn(new ShoppingCard());
        when(orderMapper.toDto(any())).thenReturn(expectedOrderDto);

        // when
        final OrderDto actual = orderService.createOrder(createOrderDto);

        // then
        assertEquals(expectedOrderDto.getOrderNumber(), actual.getOrderNumber());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(purchaseObjectRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Update order payment status by valid ID and return true")
    void updateOrderPaymentStatusAsPaidAndAddCurrentData_ValidOrderId_ShouldReturnTrue() {
        //given
        Long orderId = 1L;
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when
        final boolean actual = orderService.updateOrderPaymentStatusAsPaidAndAddCurrentData(orderId);

        //then
        assertTrue(actual);
        assertEquals(OrderPaymentStatus.PAID, order.getPaymentStatus());
        verify(orderRepository).save(order);
    }

    // Всі інші методи (findAll, getById) працюють аналогічно через моки

    private PurchaseObject getPurchaseObject() {
        PurchaseObject po = new PurchaseObject();
        po.setWine(getWine());
        po.setQuantity(1);
        po.setPrice(new BigDecimal("870"));
        return po;
    }

    private User getUser(CreateOrderDto dto) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail(dto.getEmail());
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        return user;
    }

    private Wine getWine() {
        Wine wine = new Wine();
        wine.setId(1L);
        wine.setPrice(new BigDecimal("870"));
        return wine;
    }

    private CreateOrderDto getCreateOrderDto() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setUserFirstAndLastName("Ivan Ivanov");
        dto.setEmail("ivan@test.com");
        dto.setPhoneNumber("+38000");

        CreateShoppingCardDto cardDto = new CreateShoppingCardDto();
        CreatePurchaseObjectDto poDto = new CreatePurchaseObjectDto();
        poDto.setWineId(1L);
        poDto.setQuantity(1);
        cardDto.setPurchaseObjects(Set.of(poDto));
        dto.setCreateShoppingCardDto(cardDto);

        dto.setCreateOrderDeliveryInformationDto(new CreateOrderDeliveryInformationDto());
        return dto;
    }

    private OrderDto getOrderDto(CreateOrderDto dto) {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderNumber("ORDER_ABC1");
        return orderDto;
    }
}
