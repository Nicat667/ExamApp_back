package org.example.examinationapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context; // WARNING: Make sure you import this exact Context, not the javax.naming one!

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(String to, String subject, String name, String link) {
        try {
            // 1. Prepare the variables to inject into the HTML template
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("link", link);

            // 2. Tell Thymeleaf to process "email-template.html" using those variables
            String htmlBody = templateEngine.process("email-template", context);

            // 3. Prepare the actual email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // 'true' means this is HTML!

            // 4. Send it
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}