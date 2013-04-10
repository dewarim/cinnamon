package cinnamon

import cinnamon.global.ConfThreadLocal
import com.sun.mail.smtp.SMTPTransport

import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class MailSenderService {

    public Boolean sendMail(String from, String to, String subject, String body){
        ConfThreadLocal conf = ConfThreadLocal.getConf();
        Properties props = new Properties();
        props.put("mail.smtp.host", conf.getField("mail/smtp-host", "example.invalid"));
        props.put("mail.user", conf.getField("mail/user", ""));
        props.put("mail.password", conf.getField("mail/password", ""));

        Session session = Session.getInstance(props);
        Boolean sentMsg = true;
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = [new InternetAddress(to)]
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);

            msg.setText(body);
            // set the Date: header
            msg.setSentDate(new Date());

            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
            try {
                transport.connect(
                        props.getProperty("mail.smtp.host"),
                        props.getProperty("mail.user"),
                        props.getProperty("mail.password")
                );
                transport.sendMessage(msg, msg.getAllRecipients());
            } finally {
                transport.close();
            }

        } catch (Exception ex) {
            sentMsg = false;
            log.warn("sendMail failed:\n",ex);
        }
        return sentMsg;
    }
}
