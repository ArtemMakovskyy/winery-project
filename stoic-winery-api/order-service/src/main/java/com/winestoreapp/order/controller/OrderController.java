package com.winestoreapp.order.controller;

import com.winestoreapp.common.dto.ResponseErrorDto;
import com.winestoreapp.common.exception.EntityNotFoundException;
import com.winestoreapp.common.observability.ObservationNames;
import com.winestoreapp.common.observability.ObservationTags;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.order.api.OrderService;
import com.winestoreapp.order.api.dto.CreateOrderDto;
import com.winestoreapp.order.api.dto.OrderDto;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints to managing orders")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final SpanTagger spanTagger;

    @Operation(summary = "Find all orders",
            description = """
                    Find all orders. Use size, page and sort for pagination.
                    Pagination example: /orders?size=5&page=0&sort=id
                    Available for all users""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))))
    })
    @GetMapping
    @Observed(
            name = ObservationNames.ORDER_FIND_ALL,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.READ}
    )
    public ResponseEntity<List<OrderDto>> findAllOrders(Pageable pageable) {
        log.info("REST request to find all orders with pagination: {}", pageable);
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @Operation(summary = "Find all orders by user ID",
            description = """
                    Find all orders by user ID. Use size, page and sort for pagination.
                    Pagination example: /orders/users/{userId}?size=5&page=0&sort=id
                    Available for all users""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User's orders returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))))
    })
    @GetMapping("/users/{userId}")
    @Observed(
            name = ObservationNames.ORDER_FIND_BY_USER,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.READ}
    )
    public ResponseEntity<List<OrderDto>> findAllOrdersByUserId(
            @PathVariable("userId") Long userId,
            Pageable pageable
    ) {
        log.info("REST request to find orders for userId: {}", userId);
        spanTagger.tag(ObservationTags.USER_ID, userId);
        return ResponseEntity.ok(orderService.findAllByUserId(userId, pageable));
    }

    @Operation(summary = "Add new order",
            description = """
                    Adds an order for wine from a specific User. Users are identified by first 
                    name, last name, and telephone number. If there is already a user in the 
                    database with first name, last name, and phone number, this user is linked 
                    to the order. Otherwise, the database is searched for a user by first and 
                    last name, and if one is found, a phone number is added to the found user 
                    and linked to the order. If in this case there is no user in the database, 
                    it is created anew. Available to all users.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid validation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PostMapping
    @Observed(
            name = ObservationNames.ORDER_CREATE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public ResponseEntity<OrderDto> addOrder(@RequestBody @Valid CreateOrderDto dto) {
        log.info("REST request to add new order for customer: {}", dto.getUserFirstAndLastName());
        OrderDto responseDto = orderService.createOrder(dto);
        spanTagger.tag(ObservationTags.ORDER_ID, responseDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Find order by id",
            description = "Find order by id from database. Available for all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @GetMapping("/{id}")
    @Observed(
            name = ObservationNames.ORDER_FIND_BY_ID,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.READ}
    )
    public ResponseEntity<OrderDto> getOrderById(@PathVariable("id") Long id) {
        log.info("REST request to get order by id: {}", id);
        spanTagger.tag(ObservationTags.ORDER_ID, id);
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Set the PAID status",
            description = """
                    Set the status PAID for the order and set current data.
                    Available for managers.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}/paid")
    @Observed(
            name = ObservationNames.ORDER_SET_PAID,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public ResponseEntity<Boolean> setPaidStatus(@PathVariable("id") Long id) {
        log.info("REST request to mark order {} as PAID", id);
        spanTagger.tag(ObservationTags.ORDER_ID, id);

        boolean updated = orderService.markAsPaid(id);
        if (!updated) {
            log.warn("Failed to update status: Order {} not found", id);
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete order by id",
            description = "Delete order by id from database. Available for managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    @Observed(
            name = ObservationNames.ORDER_DELETE,
            lowCardinalityKeyValues = {ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public ResponseEntity<Void> deleteOrderById(@PathVariable("id") Long id) {
        log.info("REST request to delete order by id: {}", id);
        spanTagger.tag(ObservationTags.ORDER_ID, id);

        boolean deleted = orderService.deleteById(id);
        if (!deleted) {
            log.warn("Failed to delete: Order {} not found", id);
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

}
