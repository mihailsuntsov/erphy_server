package com.dokio.service.mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dokio.message.request.mail.ProductOrder;

@Service("orderService")
public class OrderServiceImpl implements OrderService{
/*
    @Autowired
    MailService mailService;

    @Override
    public void sendOrderConfirmation(ProductOrder productOrder) {
        mailService.sendEmail(productOrder);
    }*/

}