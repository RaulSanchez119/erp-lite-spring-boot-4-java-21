package com.raulsanchez.erp_lite;

import com.raulsanchez.erp_lite.domain.order.OrderId;
import com.raulsanchez.erp_lite.domain.shared.Email;
import com.raulsanchez.erp_lite.domain.shared.Money;
import com.raulsanchez.erp_lite.persistence.mail.adapters.GmailAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.MailSender;

import java.math.BigDecimal;
import java.util.Currency;

@SpringBootApplication
public class ErpLiteApplication implements CommandLineRunner {

    @Autowired
//    private GmailAdapter gmailAdapter;

    public static void main(String[] args) {
        SpringApplication.run(ErpLiteApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        Email email = Email.of("test1@gmail.com");
//        OrderId orderId = OrderId.generate();
//        String orderNumber = "2SD-12D-55F";
//        Money money = Money.of(new BigDecimal("2999.99"), Currency.getInstance("EUR"));
//        String customerName = "Raúl Sánchez";
//        int itemsCount = 10;
//
//        this.gmailAdapter.sendMail(
//                email, orderId, orderNumber, money, customerName, itemsCount
//        );
    }
}