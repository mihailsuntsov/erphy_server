package com.laniakea.controller;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

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

import com.laniakea.message.request.LoginForm;
import com.laniakea.message.request.SignUpForm;
import com.laniakea.message.response.JwtResponse;
import com.laniakea.message.response.ResponseMessage;
import com.laniakea.model.Role;
import com.laniakea.model.RoleName;
import com.laniakea.model.User;
import com.laniakea.repository.RoleRepository;
import com.laniakea.repository.UserRepository;
import com.laniakea.security.jwt.JwtProvider;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
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


	@RequestMapping(path = "/example", method = RequestMethod.GET, headers = "X-Custom")
	public String example() {
		return "example-view-name";
	}


	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = jwtProvider.generateJwtToken(authentication);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
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