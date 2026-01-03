package com.auth.common.mail;

public interface MailService {

    void sendEmail(String to, String subject, String body);
}
