package com.winestoreapp.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.order.api.OrderService;
import com.winestoreapp.order.api.dto.CreateOrderDeliveryInformationDto;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.CreatePurchaseObjectDto;
import com.winestoreapp.order.api.dto.CreateShoppingCardDto;
import com.winestoreapp.order.api.dto.OrderDto;
import com.winestoreapp.order.config.OrderControllerTestConfig;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = {
        OrderControllerTestConfig.class,
        CustomGlobalExceptionHandler.class
})
class OrderControllerTest {
    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser
    @DisplayName("GET /orders - Success")
    void findAllOrders_ShouldReturnList() throws Exception {
        OrderDto dto = new OrderDto();
        dto.setOrderNumber("ORD-123");

        Mockito.when(orderService.findAll(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /orders/users/{userId} - Success")
    void findAllOrdersByUserId_ShouldReturnList() throws Exception {
        OrderDto dto1 = new OrderDto();
        dto1.setOrderNumber("ORD-201");
        OrderDto dto2 = new OrderDto();
        dto2.setOrderNumber("ORD-202");

        Mockito.when(orderService.findAllByUserId(anyLong(), any()))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/orders/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-201"))
                .andExpect(jsonPath("$[1].orderNumber").value("ORD-202"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /orders/{id} - Success")
    void getOrderById_ShouldReturnOrder() throws Exception {
        OrderDto dto = new OrderDto();
        dto.setOrderNumber("ORD-101");

        Mockito.when(orderService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-101"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /orders - Created")
    void addOrder_ValidDto_ShouldReturnCreated() throws Exception {
        CreateOrderDeliveryInformationDto deliveryDto = new CreateOrderDeliveryInformationDto();
        deliveryDto.setZipCode("03150");
        deliveryDto.setRegion("Kyiv");
        deliveryDto.setCity("Kyiv");
        deliveryDto.setStreet("Lobanovskogo str, 13");
        deliveryDto.setComment("None");

        CreatePurchaseObjectDto purchaseDto = new CreatePurchaseObjectDto();
        purchaseDto.setWineId(1L);
        purchaseDto.setQuantity(2);

        CreateShoppingCardDto shoppingCardDto = new CreateShoppingCardDto();
        shoppingCardDto.setPurchaseObjects(Set.of(purchaseDto));

        CreateOrderDto createDto = new CreateOrderDto();
        createDto.setUserFirstAndLastName("Ivan Petrov");
        createDto.setEmail("ivanov.ivan@gmail.com");
        createDto.setPhoneNumber("+380509876543");
        createDto.setCreateOrderDeliveryInformationDto(deliveryDto);
        createDto.setCreateShoppingCardDto(shoppingCardDto);

        OrderDto responseDto = new OrderDto();
        responseDto.setOrderNumber("ORD-NEW");

        Mockito.when(orderService.createOrder(any())).thenReturn(responseDto);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-NEW"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PATCH /orders/{id}/paid - Success for MANAGER")
    void setPaidStatus_ManagerRole_ShouldReturnOk() throws Exception {
        Mockito.when(orderService.markAsPaid(1L)).thenReturn(true);

        mockMvc.perform(patch("/orders/1/paid").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /orders/{id} - Success for MANAGER")
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        Mockito.when(orderService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/orders/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /orders/{id}/paid - Forbidden for USER")
    void setPaidStatus_UserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/orders/1/paid").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /orders/{id}/paid - Forbidden without login (CSRF)")
    void setPaidStatus_NoAuth_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/orders/1/paid"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /orders/{id} - Forbidden for USER")
    void deleteOrder_UserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/orders/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /orders/{id} - Not Found")
    void getOrderById_NotFound_ShouldReturn404() throws Exception {
        Mockito.when(orderService.getById(anyLong()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /orders - Invalid DTO")
    void addOrder_InvalidDto_ShouldReturnBadRequest() throws Exception {
        CreateOrderDto invalidDto = new CreateOrderDto();

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PATCH /orders/{id}/paid - Not Found")
    void setPaidStatus_NonExistingOrder_ShouldReturn404() throws Exception {
        Mockito.when(orderService.markAsPaid(anyLong())).thenReturn(false);

        mockMvc.perform(patch("/orders/999/paid").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /orders/{id} - Not Found")
    void deleteOrder_NonExistingOrder_ShouldReturn404() throws Exception {
        Mockito.when(orderService.deleteById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/orders/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /orders/users/{userId} - Non-existing user")
    void findAllOrdersByUserId_Empty_ShouldReturnEmptyList() throws Exception {
        Mockito.when(orderService.findAllByUserId(anyLong(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/orders/users/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /orders - Pageable edge case")
    void findAllOrders_PageableEdge_ShouldReturnOk() throws Exception {
        OrderDto dto = new OrderDto();
        dto.setOrderNumber("ORD-EDGE");

        Mockito.when(orderService.findAll(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/orders?size=0&page=-1&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-EDGE"));
    }
}
