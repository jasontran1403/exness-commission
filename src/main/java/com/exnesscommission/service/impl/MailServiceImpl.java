package com.exnesscommission.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.exnesscommission.dto.MailDto;
import com.exnesscommission.service.MailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService{
	@Autowired
    JavaMailSender sender;
	
	@Autowired
    private TemplateEngine templateEngine;

	@Override
    public void send(MailDto mail) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(mail.getFrom());
        helper.setTo(mail.getTo());
        helper.setSubject(mail.getSubject());
        helper.setReplyTo(mail.getFrom());

        // Set CC and BCC if they exist
        if (mail.getCc() != null && mail.getCc().length > 0) {
            helper.setCc(mail.getCc());
        }
        if (mail.getBcc() != null && mail.getBcc().length > 0) {
            helper.setBcc(mail.getBcc());
        }

        // Prepare the context for the template
        Context context = new Context();
        context.setVariable("subject", mail.getSubject());
        context.setVariable("body", mail.getBody());

        // Generate the email body using the template
        String htmlContent = templateEngine.process("email-template", context);
        helper.setText(htmlContent, true);

        // Send the message
        sender.send(message);
    }
}
