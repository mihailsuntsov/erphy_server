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

package com.dokio.security.services;

import com.dokio.model.User;
import com.dokio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


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

	public User getUserById(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(
				() -> new UsernameNotFoundException("User Not Found with -> id : " + userId.toString()));
		return user;
	}

//	public boolean isUserNotBlocked(LoginForm loginRequest){
//		String stringQuery;
//		stringQuery=" select count(*) from users where username='"+loginRequest.getUsername()+"' and status_account=2 ";//1-не активирован 2-активный 3-заблокирован 4-удалён
//		Query query = entityManager.createNativeQuery(stringQuery);
//		return (query.getSingleResult()).toString().equals("1");
//	}
//	public int getUserStatus(String){
//		String stringQuery;
//		stringQuery=" select status_account from users where username='"+userName+"'";//1-не активирован 2-активный 3-заблокирован 4-удалён
//		Query query = entityManager.createNativeQuery(stringQuery);
//		return ((Integer) query.getSingleResult());
//	}
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
				" select u.time_zone_id from user_settings u where u.user_id=" + getUserId() +
				")";
		Query query = entityManager.createNativeQuery(stringQuery);
		return query.getSingleResult().toString();
	}

	public String getUserTimeZone(Long userId){
		String stringQuery;
		stringQuery=    " select " +
				" s.canonical_id " +
				" from " +
				" sprav_sys_timezones s" +
				" where s.id=(" +
				" select u.time_zone_id from user_settings u where u.user_id=" + userId +
				")";
		Query query = entityManager.createNativeQuery(stringQuery);
		return query.getSingleResult().toString();
	}

}