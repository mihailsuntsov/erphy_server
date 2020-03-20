package com.laniakea.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.laniakea.security.services.UserDetailsServiceImpl;


//Фильтр, с которого начнётся процесс аутентификации пользователя.
// Фильтр должен создать (в controller/AuthRestAPIs) объект типа Authentication
// и тпм же попробовать аутентифицировать его при помощи AuthenticationManager

public class JwtAuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtProvider tokenProvider;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

	@Override //вызывается оттуда же, из OncePerRequestFilter
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
		//сначала запрос попадает сюда
			String jwt = getJwt(request);
			if (jwt != null && tokenProvider.validateJwtToken(jwt)) {
				//если юзер залогинен (jwt !=null), выполняется :
				String username = tokenProvider.getUserNameFromJwtToken(jwt);

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			logger.error("Can NOT set user authentication -> Message: {}", e);
		}

		filterChain.doFilter(request, response);
	}

	private String getJwt(HttpServletRequest request) {
//в хеадере отправляется параметр Authorization, и в нём параметр вида "Bearer <JWT-ключ>"
		String authHeader = request.getHeader("Authorization");

// если Authorization начинается с "Bearer " - убираем "Bearer ", остается ключ
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.replace("Bearer ", "");
		}

		return null;
	}
}
