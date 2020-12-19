package com.controller;

import com.pojo.Student;
import com.service.stuservice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.List;

@Controller
public class testcon {
    @Resource
    private stuservice stuservice;

    @RequestMapping("selectAllStudent")
    public String t1() throws FileNotFoundException {

        List<Student> students = stuservice.selectAllStudent();
        for (Student student : students) {
            System.out.println(student);
        }
        System.out.println("selectall执行");
        return "index";
    }

    @ResponseBody
//    @RequestBody
    @RequestMapping(value = "selectAllStudent2", produces = "application/json;charset=utf-8")
    public List t2() throws FileNotFoundException {
        List<Student> students = stuservice.selectAllStudent();
        return students;
    }
    @ResponseBody
    @RequestMapping("SessionUI")
    public void t3(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        System.out.println(request.getHeader("Referer"));
    }
}
