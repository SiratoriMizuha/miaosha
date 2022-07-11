package com.miaoshaproject.controller;

import com.alibaba.druid.sql.dialect.phoenix.visitor.PhoenixASTVisitor;
import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.catalina.User;
import org.apache.tomcat.util.security.MD5Encoder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(HttpServletRequest request, HttpServletResponse response,@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }



        UserModel userModel= userService.validateLogin(telphone,this.EncodeByMd5(password));

        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", this.httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
//                .maxAge(3600)  // 1个小时候过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build()
                ;
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return CommonReturnType.create(null);

    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")String gender,
                                     @RequestParam(name = "age")String age,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        String inSessionOtpCode= String.valueOf(this.httpServletRequest.getSession().getAttribute(telphone));
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }

        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setGender(Integer.valueOf(gender));
        userModel.setAge(Integer.valueOf(age));
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);

    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder base64en = Base64.getEncoder();
//        BASE64Encoder base64en = new BASE64Encoder();

        //加密字符串
        String newstr = base64en.encodeToString(md5.digest(str.getBytes("utf-8")));

        return newstr;
    }

    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "telphone")String telphone){
        Random random=new Random();
        int randomInt= random.nextInt(99999);
        randomInt+=10000;
        String otpCode=String.valueOf(randomInt);

        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        System.out.println("telphone="+telphone+"& optCode="+otpCode);


        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
//                .maxAge(3600)  // 1个小时候过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build()
                ;
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        if (userModel==null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        UserVO userVO= converFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    private UserVO converFromModel(UserModel userModel){
        if (userModel==null){
            return null;
        }
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;

    }






}
