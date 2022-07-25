package com.dj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class UserLoginAdapter extends WebMvcConfigurerAdapter {
    @Autowired
    UserLoginVerifyInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor).addPathPatterns("/**")   ;
        super.addInterceptors(registry);
    }
}