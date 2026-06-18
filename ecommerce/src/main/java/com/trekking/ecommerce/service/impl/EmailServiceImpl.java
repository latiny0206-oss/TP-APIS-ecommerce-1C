package com.trekking.ecommerce.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.trekking.ecommerce.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Override
    public void sendEmail(String to, String subject, String htmlContent) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY no configurada. Simulando envío de email a {}: {}", to, subject);
            return;
        }

        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Cumbre <onboarding@resend.dev>")
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email enviado exitosamente a {} con ID {}", to, data.getId());
        } catch (ResendException e) {
            log.error("Error enviando email a {} via Resend: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado enviando email a {}: {}", to, e.getMessage());
        }
    }
}
