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
    public void sendOtpEmail(String to, String name, String otpCode) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otpCode", otpCode);

            String htmlBody = templateEngine.process("otp-email-template", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your ExamPortal Verification Code");
            helper.setText(htmlBody, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send OTP email", e);
        }
    }
}