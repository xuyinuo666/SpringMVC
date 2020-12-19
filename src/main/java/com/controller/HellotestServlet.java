package com.controller;

import com.pojo.testPOJO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HellotestServlet{


    @RequestMapping("index")
    public String index(){
        return "redirect:/";
    }

    //支持占位符{}
   @RequestMapping("/hello/{id}")
    public String t1(@PathVariable ("id") String id){
       System.out.println("点我了!"+id); //点我了!1
//       return "hello";
       return "redirect:/hello";
   }
    /**
     * @RequestParam 注解用于映射请求参数
     *         value 用于映射请求参数名称
     *         required 用于设置请求参数是否必须的
     *         defaultValue 设置默认值，当没有传递参数时使用该值
     */

    @RequestMapping("/helloHasValue")
    public String t2(@RequestParam(value = "username",required = false,defaultValue = "000") String name,@RequestParam("password") String mima){
        System.out.println("点我了!"+name+mima);//点我了!000
        return "hello";
    }
    @RequestMapping("/helloHasValue2")
    public String t3(@RequestHeader(value = "Accept-Language") String head){
        System.out.println("点我了!"+head);//点我了!zh-CN,zh;q=0.9,en;q=0.8
        return "hello";
    }
    //直接将参数映射到POJO
    @RequestMapping("/helloHasValuePOJO")
    public String t4(testPOJO testPOJO){
        System.out.println("点我了!"+testPOJO);//点我了!testPOJO{username='111111111', password='11111111111'}
        //注意需要加set get方法才会生效
        return "hello";
    }
}
