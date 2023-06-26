package com.dokio.repository;
import com.dokio.model.User;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Repository
public class MailRepository {

    Logger logger = Logger.getLogger("MailRepository");

    @Value("${dokioserver.host}")
    private String dokioserver;
    @Value("${activate_account.from_email}")
    private String from_email;

    @Autowired
    JavaMailSender mailSender;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    CommonUtilites cu;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;

    public Integer activateAccount(String email, String uuid, String langCode){
        try{
            String htmlTemplate = cu.translateHTMLmessage("email_template",langCode);
            String mailLink ="<a href=\"http://"+dokioserver+"/activate/"+ uuid+"\">http://"+dokioserver+"/activate/"+ uuid +"</a>" ;
            String mailHeader = "Email confirmation";
            String mailContent="You have registered new account. Please use the below link to confirm your e-mail."+ "<br><br> Click on Link: "+mailLink;
            final String mailBody = htmlTemplate.replace("{HEADER}", mailHeader).replace("{CONTENT}",mailContent);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, email, "Email confirmation", mailBody);
            mailSender.send(messagePreparator);

            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Integer forgotPass(String email){
        try{
            User user = userRepository.findByEmail(email.trim());
            if(user==null) {logger.warn("User not found in setNewPass on email "+email.trim()); return -100;}
            String uuid = UUID.randomUUID().toString();
            user.setRepairPassCode(uuid);
            userRepository.save(user);
            String langCode = userRepositoryJPA.getUserSuffix(user.getId());
            String htmlTemplate = cu.translateHTMLmessage("email_template",langCode);
            String mailLink ="<a href=\"http://"+dokioserver+"/newpass/"+ uuid+"\">http://"+dokioserver+"/newpass/"+ uuid +"</a>" ;
            String mailContent="You have requested for a new password. Please use the below link to set new password."+ "<br><br> Click on Link: "+mailLink;
            String mailHeader = "Password recovery";
            final String mailBody = htmlTemplate.replace("{HEADER}", mailHeader).replace("{CONTENT}",mailContent);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, email, "Password recovery", mailBody);
            mailSender.send(messagePreparator);
            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Integer setNewPass(String uuid, String pwd){
        try{
            User user = userRepository.findByRepairPassCode(uuid);
            if(user==null){logger.warn("User not found in setNewPass on UUID repair pass code. UUID = "+uuid); return -101;}
            user.setPassword(encoder.encode(pwd));
            user.setRepairPassCode(null);
            userRepository.save(user);
            return 1;
        } catch (Exception e){
            logger.error("Exception in method setNewPass.", e);
            e.printStackTrace();
            return null;
        }
    }

    private MimeMessagePreparator getMessagePreparator(String from_email, String to_email, String subject, String text) {
        return mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(to_email);
            messageHelper.setFrom(from_email);
            messageHelper.setSubject(subject);
            messageHelper.setText(text, true);
        };
    }

    void sentMessage(String emailTo, String subject, String message, String langCode){
        try{
            String htmlTemplate = cu.translateHTMLmessage("email_template",langCode);
            final String mailBody = htmlTemplate.replace("{HEADER}", subject).replace("{CONTENT}",message);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, emailTo, subject, mailBody);
            mailSender.send(messagePreparator);
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method sentMessage.", e);
        }
    }



}
