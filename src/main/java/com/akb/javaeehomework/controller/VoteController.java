package com.akb.javaeehomework.controller;


import com.akb.javaeehomework.entity.Vote;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
public class VoteController {
    @Resource
    JdbcTemplate jdbcTemplate;
    @CrossOrigin
    @RequestMapping(value = "getVote",method = RequestMethod.GET)
    public String getVote(Model model, HttpServletRequest request, HttpServletResponse response){
        try {
            //要限制IP请求次数

            //限制协议
            System.out.println(request.getScheme().toUpperCase());
            if (request.getScheme().toUpperCase().indexOf("HTTPS")<0) return "";
            request.getRequestDispatcher("/vote/getvote").forward(request,response);
        }catch (Exception e){ e.printStackTrace();}
        return "vote";
    }
}
