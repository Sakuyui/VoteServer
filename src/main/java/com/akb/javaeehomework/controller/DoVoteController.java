package com.akb.javaeehomework.controller;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class DoVoteController {

    @CrossOrigin
    @RequestMapping(value = "dovote",method = RequestMethod.POST)
    public String getVote(Model model, HttpServletRequest request, HttpServletResponse response){
        try {
            //要限制IP请求次数
            //限制协议
            System.out.println(request.getScheme().toUpperCase());
            if (request.getScheme().toUpperCase().indexOf("HTTPS")<0) return "";
            request.getRequestDispatcher("/vote/dovote").forward(request,response);

        }catch (Exception e){ e.printStackTrace();}
        return "vote";
    }


}
