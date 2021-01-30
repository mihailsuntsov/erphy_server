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
package com.dokio.security.services;

import com.dokio.message.request.LoginForm;
import com.dokio.model.User;
import com.dokio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	UserRepository userRepository;


	@Override
	@Transactional
	/*переопределяем метод loadUserByUsername(), чтобы Spring Security понимал, как взять пользователя по его имени из хранилища.
	Имея этот метод, Spring Security может сравнить переданный пароль с настоящим и аутентифицировать пользователя (либо не аутентифицировать)*/
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("User Not Found with -> username or email : " + username));
		return UserPrinciple.build(user);
	}

	public Long getUserId() {
		String username = getUserName();
		if(!username.equals("anonymousUser")) {
			Long id = userRepository.findByUsername(username).get().getId();
			return id;
		}else{
			return null;
		}
	}
	public String getUserShortNameByUsername(String username) {
		if(!username.equals("anonymousUser")) {
			return userRepository.findByUsername(username).get().getName();
		}else{
			return null;
		}
	}
	public Long getUserIdByUsername(String username) {
		if(!username.equals("anonymousUser")) {
			Long id = userRepository.findByUsername(username).get().getId();
			return id;
		}else{
			return null;
		}
	}

	public User getUserByUsername(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("User Not Found with -> username or email : " + username));
		return user;
	}

	public boolean isUserNotBlocked(LoginForm loginRequest){
		String stringQuery;
		stringQuery=" select count(*) from users where username='"+loginRequest.getUsername()+"' and status_account=2 ";//1-не верифицирован 2-активный 3-заблокирован 4-удалён
		Query query = entityManager.createNativeQuery(stringQuery);
		return (query.getSingleResult()).toString().equals("1");
	}
	public boolean isUserNotBlocked_byUsername(String username){
		String stringQuery;
		stringQuery=" select count(*) from users where username='"+username+"' and status_account=2 ";//1-не верифицирован 2-активный 3-заблокирован 4-удалён
		Query query = entityManager.createNativeQuery(stringQuery);
		return (query.getSingleResult()).toString().equals("1");
	}
	public String getUserName(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getName(); // auth так же может давать список ролей юзера
	}

	public String getUserTimeZone(){
		String stringQuery;
		stringQuery=    " select " +
				" s.canonical_id " +
				" from " +
				" sprav_sys_timezones s" +
				" where s.id=(" +
				" select u.time_zone_id from users u where u.id=" + getUserId() +
				")";
		Query query = entityManager.createNativeQuery(stringQuery);
		return query.getSingleResult().toString();
	}

}