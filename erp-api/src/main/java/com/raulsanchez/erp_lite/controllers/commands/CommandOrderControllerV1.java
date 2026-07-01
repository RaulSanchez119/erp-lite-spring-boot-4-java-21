package com.raulsanchez.erp_lite.controllers.commands;

import com.raulsanchez.erp_lite.commands.order.CancelOrderCommand;
import com.raulsanchez.erp_lite.commands.order.CreateOrderCommand;
import com.raulsanchez.erp_lite.commands.order.UpdateOrderStatusCommand;
import com.raulsanchez.erp_lite.paths.ApiPaths;
import com.raulsanchez.erp_lite.use_cases.order.CancelOrderUseCase;
import com.raulsanchez.erp_lite.use_cases.order.CreateOrderUseCase;
import com.raulsanchez.erp_lite.use_cases.order.UpdateOrderStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping(path = ApiPaths.COMMANDS_ORDERS, version = "1")
@RequiredArgsConstructor
@Tag(name = "Command Orders", description = "Endpoints de comandos para gestión de órdenes")
public class CommandOrderControllerV1 {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;


    @Operation(summary = "Crear orden", description = "Crea una nueva orden de compra en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de orden inválidos"),
            @ApiResponse(responseCode = "409", description = "Violación de regla de negocio"),
            @ApiResponse(responseCode = "422", description = "Error al procesar el comando"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<Void> postOrder(
            @Parameter(description = "Datos de la orden a crear", required = true)
            @Valid @RequestBody CreateOrderCommand createOrderCommand){

        log.info("POST order");

        String productId = this.createOrderUseCase.execute(createOrderCommand);

        return ResponseEntity.created(URI.create(ApiPaths.COMMANDS_ORDERS + "/" + productId )).build();

    }

    @Operation(summary = "Cancelar orden", description = "Cancela una orden existente indicando el motivo de la cancelación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orden cancelada exitosamente"),
            @ApiResponse(responseCode = "409", description = "Violación de regla de negocio"),
            @ApiResponse(responseCode = "422", description = "Error al procesar el comando"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping(path = "/{id}/cancel")
    public ResponseEntity<Void> patchOrderCancel(
            @Parameter(description = "Identificador único de la orden", required = true, example = "order-001")
            @PathVariable String id,
            @Parameter(description = "Motivo de la cancelación", required = true, example = "Cliente solicitó cancelación")
            @RequestParam String reason){

        log.info("PATCH cancel order");

        var command = new CancelOrderCommand(id, reason);

        this.cancelOrderUseCase.execute(command);

        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "Actualizar estado de orden", description = "Cambia el estado actual de una orden (ej. PENDING, SHIPPED, DELIVERED)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estado de orden actualizado exitosamente"),
            @ApiResponse(responseCode = "409", description = "Violación de regla de negocio"),
            @ApiResponse(responseCode = "422", description = "Error al procesar el comando"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping(path = "/{id}/status")
    public ResponseEntity<Void> patchOrderStatus(
            @Parameter(description = "Identificador único de la orden", required = true, example = "order-001")
            @PathVariable String id,
            @Parameter(description = "Nuevo estado de la orden", required = true, example = "SHIPPED")
            @RequestParam String status){

        log.info("PATCH order status");

        var command = new UpdateOrderStatusCommand(id, status);

        this.updateOrderStatusUseCase.execute(command);

        return ResponseEntity.noContent().build();

    }
}