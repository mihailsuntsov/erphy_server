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

package com.dokio.security.jwt;

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

import com.dokio.security.services.UserDetailsServiceImpl;


//Фильтр, с которого начнётся процесс аутентификации пользователя.
// Фильтр должен создать (в controller/AuthRestAPIs) объект типа Authentication
// и тпм же попробовать аутентифицировать его при помощи AuthenticationManager

public class JwtAuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtProvider tokenProvider;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
		//сначала запрос попадает сюда
//			1.При каждом запросе из заголовка Authorization берем JWT-токен (он начинается с  префикса “Bearer“).
			String jwt = getJwt(request);
			//если валидация токена успешна, выполняется п.2:
			if (jwt != null && tokenProvider.validateJwtToken(jwt)) {
//				2.Извлекаем из него имя пользователя (которое записывали при формировании токена).
				String username = tokenProvider.getUserNameFromJwtToken(jwt);
//				3. Извлекаем userDetails из репо юзера по username
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//				4. Создаем authentication, и всталвяем в неё userDetails
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
