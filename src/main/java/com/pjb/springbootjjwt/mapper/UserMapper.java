package com.pjb.springbootjjwt.mapper;

import com.pjb.springbootjjwt.entity.User;
import org.apache.ibatis.annotations.Param;



public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findByUsernameAndId(@Param("username") String username,@Param("app_secret") String Id);
    User findUserById(@Param("app_secret") String Id);
}
