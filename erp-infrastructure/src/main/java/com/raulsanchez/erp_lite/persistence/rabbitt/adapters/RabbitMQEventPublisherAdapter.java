package com.raulsanchez.erp_lite.persistence.rabbitt.adapters;

import com.raulsanchez.erp_lite.domain.common.DomainEvent;
import com.raulsanchez.erp_lite.domain.entities.product.events.ProductCreated;
import com.raulsanchez.erp_lite.domain.exceptions.MyBusinessException;
import com.raulsanchez.erp_lite.domain.ports.messages.EventPublisherPort;
import com.raulsanchez.erp_lite.persistence.rabbitt.configs.RabbitMQConfig;
import com.raulsanchez.erp_lite.persistence.rabbitt.dtos.ProductCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(DomainEvent event) {

        if (event instanceof ProductCreated) {
            final var msg = this.toMsg((ProductCreated) event);

            this.rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    msg);

            log.info("Published event: {} in exchange: {}", event, RabbitMQConfig.EXCHANGE);
        } else {
            log.warn("Published event {} not supported", event);
            throw new MyBusinessException("Event is not supported");
        }
    }

    private ProductCreatedMessage toMsg(ProductCreated event) {
        return new ProductCreatedMessage(
                event.productId().value().toString(),
                event.sku().value(),
                event.name().value(),
                event.price().amount(),
                event.price().currency().getCurrencyCode(),
                event.timestamp(),
                event.description(),
                event.stock().value(),
                event.category().categoryId(),
                event.image() !=null ? event.image().imageUrl() : null,
                event.active()
        );
    }
}