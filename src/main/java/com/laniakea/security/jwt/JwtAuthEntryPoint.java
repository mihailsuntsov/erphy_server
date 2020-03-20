package com.laniakea.security.jwt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);
    
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) 
                        		 throws IOException, ServletException {
    	
        logger.error("Unauthorized error. Message - {}", e.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getOutputStream().println("{");
        response.getOutputStream().println("\"status\": " + 401 + ",");
        response.getOutputStream().println("\"error\": \"" + "Unauthorized" + "\",");
        response.getOutputStream().println("\"message\": \"" + "Error -> Unauthorized" + "\"");
        response.getOutputStream().println("}");

        //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error -> Unauthorized1");
    }
}