package com.hr.workwave.services;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {

    @Value("${workwave.service.mail.enabled}")
    private boolean mailEnabled;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String text) {
        if(mailEnabled) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("noreply@workwave.net");
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        }else{
            log.info("Mail service is disabled!");
            log.info("Mail to: {}", to);
            log.info("Mail subject: {}", subject);
            log.info("Mail body: {}", text);
        }
    }
}

//    public void sendEmail(String to, String body) {
//
//        System.out.println("Sending email to " + to + " with body: " + body);
//    }
//
////    @Value("${workwave.service.mail.enabled}")
////    private boolean mailServiceEnabled;
////
////    public EmailService() {
//
//        //if(mailServiceEnabled)
//        //insertTable
//
////        else{
////            // send.email(body,to,subject);
////        }
//    }
//
