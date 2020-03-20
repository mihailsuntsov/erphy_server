package com.laniakea.service.user;

import com.laniakea.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {

    User findByUsername(String username);

   /* void save(User user);

    Long getUserId(String username) throws UsernameNotFoundException;

    String getUserName();*/
}
