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

import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
@ComponentScan(basePackages = {"com.dokio"})
@EnableWebMvc
@EnableJpaRepositories(basePackages= "com.dokio")
@EnableTransactionManagement(proxyTargetClass = true) //иначе ошибка Bean named is expected to be of type but was actually of type com.sun.proxy
@PropertySource(value = { "classpath:application.properties" })
@Import({WebSecurityConfig.class })

public class WebConfig extends WebMvcConfigurerAdapter {
    //WebMvcConfigurerAdapter — унаследовавшись от этого класса мы получим возможность сконфигурировать ResourceLocations.

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // addResourceHandlers(ResourceHandlerRegistry registry) — переопределив данный метод,
        // мы сможем указать где будут лежать ресурсы нашего проекта, такие как css, image, js и другие.
        registry.addResourceHandler("/WEB-INF/pages/**").addResourceLocations("/pages/");
    }
    @Bean
    public InternalResourceViewResolver setupViewResolver() {
        // InternalResourceViewResolver — аналогичная конфигурация с mvc-dispatcher-servlet.xml.
        // Когда DispatcherServlet запросит у InternalResourceViewResolver'a JSP - страницу,
        // то он добавит к имени приставку /WEB-INF/pages/ и окончание .jsp
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/pages/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    // Bean for upload files
    // Bean name must be "multipartResolver", by default Spring uses method name as bean name.
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

}