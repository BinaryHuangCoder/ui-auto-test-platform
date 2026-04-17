package com.uiauto.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.uiauto.entity.User;
import com.uiauto.model.LoginRequest;
import com.uiauto.model.LoginResponse;
import java.util.Map;

public interface UserService extends IService<User> {
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户实体，不存在返回null
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户列表（用于下拉框选择）
     * @return 用户列表
     */
    java.util.List<User> listAllUsers();
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户登出
     * @param token 用户token
     */
    void logout(String token);
    
    /**
     * 刷新token
     * @param refreshToken 刷新token
     * @return 新的登录响应
     */
    LoginResponse refreshToken(String refreshToken);
}