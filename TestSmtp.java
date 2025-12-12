import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class TestSmtp {
    public static void main(String[] args) {
        String host = "smtp.gmail.com";
        final String user = "sumitmali07125@gmail.com";
        final String password = "hfka dxzu xpuk tfdf";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            System.out.println("Attempting to connect to " + host + "...");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, user, password);
            System.out.println("✅ Connection successful!");
            transport.close();
        } catch (MessagingException e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
