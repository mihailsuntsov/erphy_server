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
package com.dokio.controller;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.dokio.message.request.LoginForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.JwtResponse;
import com.dokio.message.response.ResponseMessage;
import com.dokio.model.Role;
import com.dokio.model.RoleName;
import com.dokio.model.User;
import com.dokio.repository.RoleRepository;
import com.dokio.repository.UserRepository;
import com.dokio.security.jwt.JwtProvider;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class AuthRestAPIs {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//Тут, если логин или пароль неверны, код далее не выполняется, и authenticationManager возвращает: {status: 401, error: "Unauthorized", message: "Error -> Unauthorized"}
		if(userDetailsService.isUserNotBlocked(loginRequest)) {// если пользователь не заблокирован
			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtProvider.generateJwtToken(authentication);
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
		} else return  new ResponseEntity<>(//иначе отправляем то же самое, когда логин-пароль не верны
		  "{\n" +
				"\"status\": 401,\n" +
				"\"error\": \"Unauthorized\",\n" +
				"\"message\": \"Error -> Unauthorized\"\n" +
				"}\n", HttpStatus.UNAUTHORIZED);
	}

	@SuppressWarnings("Duplicates")
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {

		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity<>(new ResponseMessage("Такой логин уже зарегистрирован"),
					HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity<>(new ResponseMessage("Такой Email уже зарегистрирован"),
					HttpStatus.BAD_REQUEST);
		}

		// Если такого логина и емайла нет
		// Создание аккаунта для нового пользователя
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		//Set<String> strRoles = signUpRequest.getRole();
		Set<String> strRoles= new HashSet<>();

		strRoles.add("admin");
		//подразумевалось, что роли будут присылаться в JSON, типа
//		{
//			"name": "Игорь Иванов",
//			"username": "iivanov",
//			"email": "iivanov@gmail.com",
//			"role": [
//			"user",
//			"admin"
//			],
//			"password": "Holls89Hy"
//		}
		//и данные конструкции сделаны под это
		// Но так как сейчас в системе из формы регистрации создаются только Админы
		// (а остальных пользователей создают уже сами Админы из системы)
		// то роль Админа из формы регистрации не передаётся, а присваивается тут

		Set<Role> roles = new HashSet<>();

		strRoles.forEach(role -> {
			switch (role) {
			case "admin":
				Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
				roles.add(adminRole);

				break;
			case "pm":
				Role pmRole = roleRepository.findByName(RoleName.ROLE_PM)
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
				roles.add(pmRole);

				break;
			default:
				Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
				roles.add(userRole);
			}
		});
//добавили юзеру сет ролей и сохранили его
		user.setRoles(roles);
		//userRepository.save(user);
		Long createdUserId=userRepository.save(user).getId();//и сохранили его
		//ответ сервера при удачном создании юзера
		//return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
		ResponseEntity<String> responseEntity = new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
		return responseEntity;
	}
}