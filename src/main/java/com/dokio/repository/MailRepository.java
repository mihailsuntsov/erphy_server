package com.dokio.repository;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.model.User;
import com.dokio.util.CommonUtilites;
import org.apache.commons.jexl3.JxltEngine;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class MailRepository {

    Logger logger = Logger.getLogger("MailRepository");

    @Value("${dokioserver.host}")
    private String systemserver;
    @Value("${activate_account.from_email}")
    private String from_email;


    @PersistenceContext
    private EntityManager entityManager;
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
            Map<String, String> map = cu.translateFromLanguage(langCode, new String[]{"'email_confirmation'","'you_registered_new_acc'","'click_link'"});
            String mailLink ="<a href=\"http://"+systemserver+"/activate/"+ uuid+"\">http://"+systemserver+"/activate/"+ uuid +"</a>" ;
            String mailHeader = map.get("email_confirmation");
            String mailContent=map.get("you_registered_new_acc")+ "<br><br> "+map.get("click_link")+": "+mailLink;
            final String mailBody = htmlTemplate.replace("{HEADER}", mailHeader).replace("{CONTENT}",mailContent);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, email, map.get("email_confirmation"), mailBody);
            mailSender.send(messagePreparator);
            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public Integer newSaasUserRegistered(String adminEmail, String userEmail, String supportLogin, String supportPassword,  String langCode){
        try{
            String htmlTemplate = cu.translateHTMLmessage("email_template",langCode);
            String mailHeader = "New account is created";
            String mailContent="User email: "+ userEmail +"<br>Support login: "+supportLogin+" <br>Support password: "+supportPassword;
            final String mailBody = htmlTemplate.replace("{HEADER}", mailHeader).replace("{CONTENT}",mailContent);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, adminEmail, "New account is created", mailBody);
            mailSender.send(messagePreparator);

            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public Integer forgotPass(String email){
        try{
            final User user;
            if(email.contains("repair_support_user")){
                // To repair support user email - need to send query with email "<login of support user>@repair_support_user.com"
                user = userRepository.findByUsername(email.substring(0,email.indexOf('@'))).get();
                if(lastRepairPassQueryAgo(user.getId())<60)
                    return -105; //Request rate exceeded Превышена частота запросов (more than 1 per 60 sec)
                if(Objects.isNull(user)) {
                    logger.warn("User not found in forgotPass by username "+email.substring(0,email.indexOf('@')));
                    return -100;
                }
                if(user==null) {logger.warn("User not found in setNewPass by Username "+email.substring(0,email.indexOf('@'))); return -100;}
                // as "<login of support user>@repair_support_user.com" is not a real email - need to set real admin email
                SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral(true);
                email = settingsGeneral.getStores_alert_email();
            } else {
                user = userRepository.findByEmail(email.trim());
                if(lastRepairPassQueryAgo(user.getId())<60)
                    return -105; //Request rate exceeded Превышена частота запросов (more than 1 per 60 sec)
                if(Objects.isNull(user)) {
                    logger.warn("User not found in forgotPass by Email "+email.trim());
                    return -100;
                }

            }

            String uuid = UUID.randomUUID().toString();
            user.setRepairPassCode(uuid);
            user.setRepair_pass_code_sent(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            String langCode = userRepositoryJPA.getUserSuffix(user.getId());
            Map<String, String> map = cu.translateFromLanguage(langCode, new String[]{"'password_recovery'","'you_rereqested_new_pwd'","'click_link'"});
            String htmlTemplate = cu.translateHTMLmessage("email_template",langCode);
            String mailLink ="<a href=\"http://"+systemserver+"/newpass/"+ uuid+"\">http://"+systemserver+"/newpass/"+ uuid +"</a>" ;
            String mailContent=map.get("you_rereqested_new_pwd")+ "<br><br> "+map.get("click_link")+": "+mailLink;
            String mailHeader = map.get("password_recovery");
            final String mailBody = htmlTemplate.replace("{HEADER}", mailHeader).replace("{CONTENT}",mailContent);
            MimeMessagePreparator messagePreparator = getMessagePreparator(from_email, email, map.get("password_recovery"), mailBody);
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

    private Long lastRepairPassQueryAgo(Long userId) throws Exception {
        try {
            String stringQuery;
            stringQuery = "SELECT coalesce(cast(EXTRACT(EPOCH FROM (now() - (select repair_pass_code_sent from users where id="+userId+"))) as bigint),3600)";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method lastRepairPassQueryAgo. User id = " +userId.toString() , e);
            throw new Exception();
        }
    }

    private MimeMessagePreparator getMessagePreparator(String from_email, String to_email, String subject, String text) {
        return mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
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
