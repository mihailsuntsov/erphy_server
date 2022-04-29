package com.dokio.service.user;

import com.dokio.model.User;
import com.dokio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userDao;

    @Override
    public User findByUsername(String username) {
        return null;
    }


}
