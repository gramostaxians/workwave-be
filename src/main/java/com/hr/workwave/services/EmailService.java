package com.hr.workwave.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {

    /**
     * Sends an email asynchronously using HTML content.
     * If email sending is disabled via configuration (workwave.service.mail.enabled = false),
     * the email content is logged for debugging purposes instead.
     *
     * @param to           Recipient's email address
     * @param subject      Email subject
     * @param htmlContent  Email body in HTML format
     */

    @Value("${workwave.service.mail.enabled}")
    private boolean mailEnabled;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        if (mailEnabled) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(to);
                helper.setFrom("noreply@workwave.net");
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
            } catch (MessagingException e) {
                log.error("Failed to send email to " + to, e);
            }
        } else {
            log.info("Mail service is disabled!");
            log.info("Mail to: {}", to);
            log.info("Mail subject: {}", subject);
            log.info("Mail body: {}", htmlContent);
        }
    }

}
