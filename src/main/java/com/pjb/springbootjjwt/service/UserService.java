package com.pjb.springbootjjwt.service;

import com.pjb.springbootjjwt.entity.User;
import com.pjb.springbootjjwt.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("UserService")
public class UserService {
    @Autowired
    UserMapper userMapper;
    public User findByUsername(User user){
        return userMapper.findByUsername(user.getUsername());
    }
    public User findByUsernameAndId(User user){
        return userMapper.findByUsernameAndId(user.getUsername(),user.getApp_secret());
    }
    public User findUserById(String userId) {
        return userMapper.findUserById(userId);
    }

}
