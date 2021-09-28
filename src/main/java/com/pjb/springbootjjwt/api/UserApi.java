package com.pjb.springbootjjwt.api;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.fastjson.JSONObject;
import com.pjb.springbootjjwt.annotation.UserLoginToken;
import com.pjb.springbootjjwt.entity.User;
import com.pjb.springbootjjwt.service.TokenService;
import com.pjb.springbootjjwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
//@RequestMapping("api")
public class UserApi {
    @Autowired
    UserService userService;
    @Autowired
    TokenService tokenService;
    //登录
    @PostMapping("/getToken")
    public Object login(@RequestBody User user){
        System.out.println("username=>"+user.toString());
        JSONObject jsonObject=new JSONObject();
        User userForBase=userService.findByUsername(user);
        Map<String,String> tokenMap = new HashMap<String,String>();
        if(userForBase==null){
            jsonObject.put("message","登录失败,用户不存在");
            jsonObject.put("success", false);
            return jsonObject;
        }else {
            System.out.println("userForBase=>"+userForBase.toString());
            if (!userForBase.getPassword().equals(user.getPassword())){
                jsonObject.put("message","登录失败，密码错误");
                jsonObject.put("success", false);
                return jsonObject;
            }
            if (!userForBase.getApp_secret().equals(user.getApp_secret())){
                jsonObject.put("message","登录失败，secret验证错误");
                jsonObject.put("success", false);
                return jsonObject;
            }
            String token = tokenService.getToken(userForBase);
            tokenMap.put("token", token);
            tokenMap.put("expires_in","7200");
            jsonObject.put("result",tokenMap);
            jsonObject.put("message","获取token成功");
            jsonObject.put("success", true);
            return jsonObject;

        }
    }
    
   
}
