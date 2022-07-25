package com.dj.controller;

import com.dj.utli.CommonUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Controller
public class LoginController {
//    @PostMapping("/login")
//    public String login(@RequestParam("email") String email, @RequestParam("password") String password) {
//       if(email.equals("jiedeng0117@gmail.com") && password.equals("123456aaa") || email.equals("1959729097@qq.com") && password.equals("123456aaa")){
//
//           return "index";
//       }
//       return "login";
//    }

    @RequestMapping("/")
    public String index(){
        return "login";
    }

//    @Configuration
//    public class DefaultView extends WebMvcConfigurerAdapter {
//        @Override
//        public void addViewControllers(ViewControllerRegistry registry) {
//            registry.addViewController("/").setViewName("login");
//            registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
//            super.addViewControllers(registry);
//        }
//    }
}
