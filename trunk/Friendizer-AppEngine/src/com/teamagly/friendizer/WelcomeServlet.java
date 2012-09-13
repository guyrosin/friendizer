package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.restfb.*;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class WelcomeServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(WelcomeServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userID = request.getParameter("userID");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User user = null;
		try {
			user = pm.getObjectById(User.class, Long.valueOf(userID));
		} catch (JDOObjectNotFoundException e) {
			log.severe(e.getMessage());
			return;
		}
		String accessToken = user.getToken();
		String userName = user.getName();
		String firstName = userName.substring(0, userName.indexOf(" "));
		String email = getEmailFromFacebook(userID, accessToken);
		log.info("access token: " + accessToken);
		log.info("email: " + email);
		if (email != null && email.length() > 0) {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			StringBuilder sb = new StringBuilder();
			sb.append("Hi ")
					.append(firstName)
					.append(",<br>I am one of the developers of Friendizer and I noticed that you downloaded our app today.<br>")
					.append("I wanted to thank you for trying it out. The app is still under heavy development and we hope that you find it useful. You are one of the first to check it out!<br>")
					.append("I'd love to hear how your initial experience was. Any feedback, ideas on how we can improve or any problems that you run into and we will fix them as soon as possible.<br><br>")
					.append("Thanks again,<br>")
					.append("Guy<br>")
					.append("friendizer.team@gmail.com");

			try {
				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress("friendizer.team@gmail.com", "Friendizer"));
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email, userName));
				msg.setSubject("Welcome to Friendizer");

				Multipart mp = new MimeMultipart();
				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(sb.toString(), "text/html");
				mp.addBodyPart(htmlPart);
				msg.setContent(mp);
				Transport.send(msg);

			} catch (AddressException e) {
				log.severe(e.getMessage());
			} catch (MessagingException e) {
				log.severe(e.getMessage());
			}
		}
	}

	private String getEmailFromFacebook(String userID, String accessToken) {
		FacebookClient facebook = new DefaultFacebookClient(accessToken);
		com.restfb.types.User user = facebook.fetchObject(userID, com.restfb.types.User.class, Parameter.with("fields", "email"));
		return user.getEmail();
	}
}