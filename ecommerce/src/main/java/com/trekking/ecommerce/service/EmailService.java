package com.trekking.ecommerce.service;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlContent);
}
