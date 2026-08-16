package tn.econstruction.notification;

public interface EmailService {
    void send(String recipient, EmailContent content);
}
