package com.dokio.service.user;

import com.dokio.model.User;

public interface UserService {

    User findByUsername(String username);

   /* void save(User user);

    Long getUserId(String username) throws UsernameNotFoundException;

    String getUserName();*/
}
