package com.raulsanchez.erp_lite.domain.ports.services;

import com.raulsanchez.erp_lite.domain.entities.order.OrderId;
import com.raulsanchez.erp_lite.domain.shared.Email;
import com.raulsanchez.erp_lite.domain.shared.Money;

/**
 * Port for email service in order created
 */
public interface OrderConfirmEmailServicePort {

    void sendMail(Email email, OrderId orderId, String orderNumber, Money money, String customerName, Integer itmesCount);
}
