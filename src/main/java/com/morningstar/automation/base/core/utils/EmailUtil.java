package com.morningstar.automation.base.core.utils;

import com.morningstar.automation.base.core.beans.MailSenderInfo;
import com.morningstar.automation.base.core.configurations.Environment;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

    private static final Logger logger = Logger.getLogger(EmailUtil.class);

    public static void sendHtmlMail(MailSenderInfo mailInfo) throws UnsupportedEncodingException {
        Properties pro = mailInfo.getProperties();
        Session sendMailSession = Session.getInstance(pro);
        sendMailSession.setDebug(true);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            InternetAddress address = new InternetAddress(mailInfo.getFromAddress(), mailInfo.getFromName());
            mailMessage.setFrom(address);

            List<String> toList = mailInfo.getToAddress();
            for (String tempTo : toList) {
                InternetAddress to = new InternetAddress(tempTo);
                mailMessage.addRecipient(Message.RecipientType.TO, to);
            }

            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new Date());
            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            BodyPart html1 = new MimeBodyPart();
            html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            String filename = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
            DataSource source = new FileDataSource(filename);
            html1.setDataHandler(new DataHandler(source));
            html1.setFileName(Environment.getTeamName() +"EnvironmentReadinessCheck.html");
            mainPart.addBodyPart(html1);
            mailMessage.setContent(mainPart);
            Transport transport = null;
            transport = sendMailSession.getTransport("smtp");
            mailMessage.saveChanges();
            transport.connect(mailInfo.getMailServerHost(), mailInfo.getUserName(), mailInfo.getPassword());
            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
            transport.close();
            logger.info("send email success!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

