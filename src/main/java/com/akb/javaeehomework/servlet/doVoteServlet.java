package com.akb.javaeehomework.servlet;


import com.akb.javaeehomework.RSACoder;
import com.akb.javaeehomework.Tools;
import com.alibaba.druid.util.Base64;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(urlPatterns = "/vote/dovote")
public class doVoteServlet extends HttpServlet {
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    RedisTemplate redisTemplate;
    //服务器1的公钥
    private static String pub="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdecY14+jjhTnqx4dRFvFx/q7t" +
            "PGpu5UkaUHc6PkDyoPCJMrqjFxVo2EXgSfc+C3OVx21NrR+PYfZQv4gZ4gBMjbs1" +
            "5offFFEaSlR+G37wkI8lpSgK2RpgRPd5TD89c0wj6AVVHwRN/DJy4rLc1JmW9N6D" +
            "F/kG80aopNeU825YgQIDAQAB";
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject resultObject=new JSONObject();
        JSONObject inputJson=null;
        PrintWriter pw=response.getWriter();
        BufferedReader br=new BufferedReader(new InputStreamReader(((HttpServletRequest)request).getInputStream()));


        /*redisTemplate.delete("voted:18260829239");
        if(true){
            return;
        }*/

        //输入参数数据读取
        String line;
        StringBuilder sb=new StringBuilder();
        while((line=br.readLine())!=null){
            sb.append(line);
        }

        inputJson= JSONObject.parseObject(sb.toString());
        System.out.println(inputJson);



        //验证签名
        boolean isSignTrue= RSACoder.verify(
                inputJson.getString("time"),
                inputJson.getString("sign"),pub,"utf-8");
        System.out.println("验证签名...."+isSignTrue);
        if(isSignTrue){
            Date now=new Date();
            SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try{
            if((now.getTime()-formatter.parse(inputJson.getString("time")).getTime())>1000*120){
                isSignTrue=false;   //签名过期
            }

            }catch (Exception e){
                isSignTrue=false;
            }
        }
        if(!isSignTrue){
            resultObject.put("code",-1);
            resultObject.put("result:","sign error");
            pw.write(resultObject.toJSONString());
        }




        //验证key1 与 key2
        System.out.print("验证key1,key2....");
        String key1="";
        String key2="";
        String phone=inputJson.getString("phone");
        key1=inputJson.getString("key1");
        key2=inputJson.getString("key2");
        System.out.print("get key1="+key1+",key2="+key2+"  ");
        if(key1==null || key2==null || phone==null ||"".equals(phone)|| "".equals(key1) || "".equals(key2))
        {
            resultObject.put("code",-2);
            resultObject.put("result:","key error");
            System.out.println("error....");
            pw.write(resultObject.toJSONString());
            return;
        }
        else{
            //key2是 加密过的手机号(base64)
            byte[] ciphertext= Base64.base64ToByteArray(key2);
            try {
                String plaintext = new String((Tools.decrypt(ciphertext, "akbsakuy")));
                System.out.print("phone="+plaintext+"  ");
                if(!(phone.equals(plaintext))){throw new Exception();}
            }catch (Exception e){
                resultObject.put("code",-2);
                resultObject.put("result:","key error");
                System.out.println("error....");
                pw.write(resultObject.toJSONString());
                return;
            }



        }

        System.out.println("true");

        //判断手机号是否已投票

        //Test use
        redisTemplate.delete("voted:"+phone);

        System.out.print("验证手机号是否已投票....");
        System.out.println("voted:"+phone);
        if(redisTemplate.opsForValue().get("voted:"+phone)!=null){
            resultObject.put("code",-3);
            resultObject.put("result:","Already voted");
            pw.write(resultObject.toJSONString());
            System.out.println("error");
            return;
        }

        System.out.println("pass");


        //判断验证码是否正确
        String vcode=inputJson.getString("vcode");
        System.out.print("验证验证码是否正确....");
        redisTemplate.opsForValue().set("votekey:"+phone,vcode);
        String vrifyCodeInput=inputJson.getString("vcode");
        String tid=inputJson.getString("tid");
        if(tid==null || "".equals(tid) || vrifyCodeInput==null){
            resultObject.put("code",-4);
            resultObject.put("result:","information error");
            pw.write(resultObject.toJSONString());
            System.out.println("...false");
            return;
        }else{
            String realVerifyCode=String.valueOf(redisTemplate.opsForValue().get("votekey:"+phone));
            if(realVerifyCode==null || !realVerifyCode.equals(vrifyCodeInput)){
                resultObject.put("code",-5);
                resultObject.put("result:","verify code error or out of date");
                pw.write(resultObject.toJSONString());
                System.out.println("...false");
                return;
            }
        }
        System.out.println("...true");

        //投票操作
        //jdbcTemplate.execute("LOCK TABLES teachervote WRITE");
        System.out.println("Begin vote");
        boolean updatesuccess=
                (jdbcTemplate.update("UPDATE teachervote set votecount=votecount+1 WHERE tID=?",tid))>0;
         //不用加锁，mysql对Update自动加锁

        //InnoDB会根据事务隔离级别自动加锁，当然程序员可以手动加锁，但通常手动加锁不但没有必要还会影响性能；
        // InnoDB的行级锁已经经过许多优化了。 ----来自高性能MySQL

        //jdbcTemplate.execute("UNLOCK TABLES");
        if(!updatesuccess){
            resultObject.put("code",-6);
            resultObject.put("result:","update database failed.You can try again");
            pw.write(resultObject.toJSONString());
            System.out.println("vote error");
            return;
        }

        System.out.println("vote success.update cache");
        //更新数据库成功，更新缓存
        redisTemplate.opsForValue().set("voted:"+phone,1);


        //返回成功结果
        System.out.println("all success");
        resultObject.put("code",1);
        resultObject.put("result:","success");
        pw.write(resultObject.toJSONString());
    }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
            doPost(request,response);
        }
}
