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
package com.dokio.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = {"com.dokio"})

public class MailConfig extends WebMvcConfigurerAdapter {

    @Value("${mail.host}")
    private String host;
    @Value("${mail.port}")
    private int port;
    @Value("${mail.username}")
    private String username;
    @Value("${mail.password}")
    private String password;
    @Value("${mail.protocol}")
    private String protocol;
    @Value("${mail.smtp.auth}")
    private String auth;
    @Value("${mail.smtp.starttls.enable}")
    private String starttls_enable;
    @Value("${mail.smtp.starttls.required}")
    private String starttls_required;
    @Value("${mail.debug}")
    private String debug;
    @Value("${mail.smtp.socketFactory.fallback}")
    private String fallback;

    // Bean for e-mail sending
    @Bean
    public JavaMailSender getMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//
//        mailSender.setUsername("noreply.dokio@gmail.com");    // your username. for example, "noreply"
//        mailSender.setPassword("Jifl8_ukd68vcp");   // your password
//        Properties props = new Properties();
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class",
//                "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "465");



        mailSender.setHost(host);       // address of SMTP mail server for example, "localhost"
        mailSender.setPort(port);                    // port of SMTP mail server
        mailSender.setUsername(username);    // your username. for example, "noreply@localhost"
        mailSender.setPassword(password);   // your password
        Properties mailProperties = new Properties();
        mailProperties.put("mail.transport.protocol", protocol);
        mailProperties.put("mail.debug", debug);
        mailProperties.put("mail.smtp.auth", auth);
        mailProperties.put("mail.smtp.starttls.enable", starttls_enable);
        mailProperties.put("mail.smtp.starttls.required", starttls_required);
        mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailProperties.put("mail.smtp.socketFactory.fallback", fallback);

        return mailSender;
    }

}