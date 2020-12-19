package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class loginController {
    @PostMapping("tologin")
    public String login(@RequestParam("Myname") String name,
                        @RequestParam("Mypass") String pass,
                        HttpSession session){
        System.out.println(name+pass);
        if ("xugaowei".equals(name)&&"111".equals(pass)){
            session.setAttribute("loginName",name);
            return "main";
        }
        return "userlogin";
    }
}
