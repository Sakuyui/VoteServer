package com.akb.javaeehomework.servlet;

import com.akb.javaeehomework.entity.Vote;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@CrossOrigin
@WebServlet(urlPatterns = "/vote/getvote")

public class getVoteServlet extends HttpServlet {
    @Resource
    JdbcTemplate jdbcTemplate;
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter pw=response.getWriter();
        List<Map<String, Object>> rs = jdbcTemplate.queryForList("select * from teachervote");
        JSONArray jsonArray=new JSONArray();
        for(Map<String,Object> m:rs){
            Vote v=new Vote();

            String tname="";
            String tid="";
            for(String k:m.keySet()){
                switch (k){
                    case "tID":{
                        tid=String.valueOf(m.get(k));
                        break;
                    }
                    case "tName":{
                        tname=((String)m.get(k));
                        break;
                    }
                    case "votecount":{
                        v.setVotecount((Integer) m.get(k));
                    }
                }
            }
            v.setName(tid+":"+tname);
            //System.out.println("["+v.getName()+","+v.getVotecount()+"]");
            JSONObject teachervoteitem=new JSONObject();
            teachervoteitem.put("tid",tid);
            teachervoteitem.put("tname",tname);
            teachervoteitem.put("vote",v.getVotecount());
            jsonArray.add(teachervoteitem);

        }


        pw.write(jsonArray.toJSONString());
        request.getSession().setAttribute("votedata",jsonArray);
        //System.out.println(jsonArray.toJSONString());

        //制作JSON

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);


        //制作JSON

    }
}


