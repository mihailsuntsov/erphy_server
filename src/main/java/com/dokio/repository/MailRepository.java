/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/
package com.dokio.repository;
import com.dokio.model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
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

    public Integer activateAccount(String email, String uuid){
        try{
            String mailLink ="http://"+dokioserver+"/activate/"+ uuid ;
            String mailBody="\n\n You have registered new account in the DokioCRM application. Please use the below link to confirm your e-mail."+ "\n\n Click on Link: "+mailLink;
            MimeMessagePreparator preparator = getMessagePreparator(from_email,email,"DokioCRM registration confirm",mailBody);
            mailSender.send(preparator);
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
            String mailLink ="http://"+dokioserver+"/newpass/"+ uuid ;
            String mailBody="\n\n You have requested for a new password from the Dokio application. Please use the below link to set new password."+ "\n\n Click on Link: "+mailLink;
            MimeMessagePreparator preparator = getMessagePreparator(from_email,email,"Password repairing",mailBody);
            mailSender.send(preparator);
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
            mimeMessage.setFrom(from_email);
            mimeMessage.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(to_email));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(text);
        };
    }

    public Integer sentMessage(String emailTo, String subject, String message){
        try{
            String mailBody=message;
            MimeMessagePreparator preparator = getMessagePreparator(from_email,emailTo,subject,mailBody);
            mailSender.send(preparator);
            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public Integer sentMessage(String emailFrom, String emailTo, String subject, String message){
        try{
            String mailBody=message;
            MimeMessagePreparator preparator = getMessagePreparator(emailFrom,emailTo,subject,mailBody);
            mailSender.send(preparator);
            return 1;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
