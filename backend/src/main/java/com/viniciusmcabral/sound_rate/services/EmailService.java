package com.viniciusmcabral.sound_rate.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private SendGrid sendGridClient;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Value("${app.sendgrid.from-email}")
	private String fromEmail;

	private void logEmailResponse(String emailType, String to, Response response) {
		if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
			logger.info("{} email sent to '{}' with status {}.", emailType, to, response.getStatusCode());
			return;
		}

		logger.warn("{} email to '{}' returned status {}.", emailType, to, response.getStatusCode());
	}

	@Async
	public void sendWelcomeEmail(String to, String username) {
		Context context = new Context();
		context.setVariable("username", username);
		sendEmail("Welcome", to, "Welcome to SoundRate!", "welcome-email", context);
	}

	@Async
	public void sendAccountDeletionEmail(String to, String username) {
		Context context = new Context();
		context.setVariable("username", username);
		sendEmail("Account deletion", to, "Your SoundRate Account Has Been Deleted", "deletion-email", context);
	}

	@Async
	public void sendPasswordResetEmail(String to, String username, String resetLink) {
		Context context = new Context();
		context.setVariable("username", username);
		context.setVariable("resetLink", resetLink);
		sendEmail("Password reset", to, "SoundRate - Password Reset Request", "password-reset-email", context);
	}

	private void sendEmail(String emailType, String to, String subject, String templateName, Context context) {
		String htmlContent = templateEngine.process(templateName, context);
		Mail mail = new Mail(new Email(fromEmail, "SoundRate"), subject, new Email(to),
				new Content("text/html", htmlContent));
		Request request = new Request();

		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			logEmailResponse(emailType, to, sendGridClient.api(request));
		} catch (IOException exception) {
			logger.error("Failed to send {} email to '{}': {}.", emailType.toLowerCase(), to, exception.getMessage());
		}
	}
}
