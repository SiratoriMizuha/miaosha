package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /*
    telphone:用户注册手机
    password:用户的加密后密码
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;
}
