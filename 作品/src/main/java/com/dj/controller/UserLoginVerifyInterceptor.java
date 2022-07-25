package com.dj.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

@Configuration
public class UserLoginVerifyInterceptor implements HandlerInterceptor {
        //    在请求处理之前调用,只有返回true才会执行请求
        @Override
        public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
//        得到session
            HttpSession session = httpServletRequest.getSession(true);
//        得到对象
            Object admin = session.getAttribute("login");
//        判断对象是否存在
            if(admin!=null && !admin.equals("")){
                return true;
            }else{
//            不存在则跳转到登录页
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
                System.out.println(httpServletRequest.getContextPath()+"/login");
                return false;
            }
        }

        //    试图渲染之后执行
        @Override
        public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        }

        //    在请求处理之后,视图渲染之前执行
        @Override
        public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

        }
    }
