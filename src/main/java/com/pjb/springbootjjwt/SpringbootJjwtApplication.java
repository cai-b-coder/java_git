package com.pjb.springbootjjwt;

import com.teamcenter.clientx.AppXSession;
import com.teamcenter.soa.client.model.strong.User;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.pjb.springbootjjwt.mapper")
public class SpringbootJjwtApplication {
    public static String TC_URL;
    public static String TC_USER;
    public static String TC_PWD;
    public static String TC_FMSURL;
    public static String TC_FCCCACH;


    public static void main(String[] args) {
        ConfigurableApplicationContext contenxt = SpringApplication.run(SpringbootJjwtApplication.class, args);
        TC_URL = contenxt.getEnvironment().getProperty("teamcenter.url");
        TC_USER = contenxt.getEnvironment().getProperty("teamcenter.user");
        TC_PWD = contenxt.getEnvironment().getProperty("teamcenter.pwd");
        TC_FMSURL = contenxt.getEnvironment().getProperty("teamcenter.fmsurl");
        TC_FCCCACH = contenxt.getEnvironment().getProperty("teamcenter.fcccash");


    }
}
