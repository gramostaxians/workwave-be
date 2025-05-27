package com.hr.workwave.services;

import org.springframework.beans.factory.annotation.Value;

public class EmailService {

    public void sendEmail(String to, String body) {

        System.out.println("Sending email to " + to + " with body: " + body);
    }

//    @Value("${workwave.service.mail.enabled}")
//    private boolean mailServiceEnabled;
//
//    public EmailService() {

        //if(mailServiceEnabled)
        //insertTable

//        else{
//            // send.email(body,to,subject);
//        }
    }

