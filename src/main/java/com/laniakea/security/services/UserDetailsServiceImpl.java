package com.laniakea.security.services;

import com.laniakea.model.User;
import com.laniakea.repository.UserRepository;
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
import java.util.Optional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("User Not Found with -> username or email : " + username));
		return UserPrinciple.build(user);
	}

	public Long getUserId() {
		String username = getUserName();
		if(!username.equals("anonymousUser")) {
//			String stringQuery = "select p.id from users p where username="+username;
//			Query query = entityManager.createNativeQuery(stringQuery);
			Long id = userRepository.findByUsername(username).get().getId();
			return id;
			//return Long.valueOf(query.getFirstResult());
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