package com.laniakea.service.user;

import com.laniakea.model.User;
import com.laniakea.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userDao;

    @Override
    public User findByUsername(String username) {
        return null;
    }





}
