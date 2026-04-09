package api_tracking.Utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;
import java.io.File;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class EmailSender {

    /**
     * Sending email with attachment to multiple recipients
     *
     * @param toEmails       array of recipient email addresses
     * @param excelFilePath  path to Excel file to attach
     */
    public static void sendEmailWithAttachment(String[] toEmails, String excelFilePath) {

        // Your Gmail account
    	Dotenv dotenv = Dotenv.load();

    	final String fromEmail = dotenv.get("EMAIL_USERNAME");
    	final String password = dotenv.get("EMAIL_PASSWORD");
    	//final String fromEmail = " avo.test@avoautomation.com";
       // final String password = " s@61K^$KOYXZ";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));

            // Convert String[] to InternetAddress[]
            InternetAddress[] addresses = new InternetAddress[toEmails.length];
            for (int i = 0; i < toEmails.length; i++) {
                addresses[i] = new InternetAddress(toEmails[i]);
            }
            message.setRecipients(Message.RecipientType.TO, addresses);

            message.setSubject(" API Tracking Automation Report - Latest Run");

            // Email body in HTML
            String htmlMessage = 
            	    "<h3>Hello,</h3>" +
            	    "<p>Please find attached the latest <b>API Tracking Automation Report</b>.</p>" +
            	    "<p>The report includes:</p>" +
            	    "<ul>" +
            	    	"<li>Build and environment information</li>" +
            	        "<li>Complete execution of all API test modules</li>" +
            	        "<li>Detailed API response summary</li>" +
            	        "<li>Captured response times and performance metrics</li>" +
            	    "</ul>" +
            	    "<p>Regards,<br>" +
            	    "<b>Automation System</b><br>" +
            	    "<i>(No-Reply Email)</i></p>";


            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlMessage, "text/html");

            // Attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File file = new File(excelFilePath);

            if (!file.exists()) {
                System.out.println("OOPs! Excel file not found: " + excelFilePath);
                return;
            }

            attachmentPart.attachFile(file);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);
            message.setContent(multipart);

            Transport.send(message);

            System.out.println(" Email sent successfully to all recipients with attachment!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" OOPs! Failed to send email");
        }
    }
}
