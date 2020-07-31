/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
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



/*
@Configuration — собственно эта аннотация и говорит о том, что данный класс является Java Configuration;
@EnableWebMvc — эта аннотация разрешает нашему проекту использовать MVC;
@ComponentScan(«com.dokio») — аналогично тому component-scan который был в mvc-dispatcher-servlet.xml, говорит, где искать компоненты проекта.
@Bean — указывает на то что это инициализация бина, и он будет создан с помощью DI.
*/

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

    //Бин для загрузки файлов
    // Bean name must be "multipartResolver", by default Spring uses method name as bean name.
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}