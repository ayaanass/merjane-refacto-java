package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.services.implementations.OrderProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


// Controller pour gérer les commandes (orders)
@RestController
@RequestMapping("/v2/orders")
public class OrderController {

    private final OrderProcessingService orderProcessingService;

    @Autowired
    public OrderController(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    @PostMapping("/{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable @NotNull Long orderId) {
        Order order = orderProcessingService.processOrder(orderId);
        return new ProcessOrderResponse(order.getId());
    }
}
