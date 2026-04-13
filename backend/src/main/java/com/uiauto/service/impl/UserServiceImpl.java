package com.uiauto.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uiauto.entity.User;
import com.uiauto.mapper.UserMapper;
import com.uiauto.model.LoginRequest;
import com.uiauto.model.LoginResponse;
import com.uiauto.model.LoginResponse.UserInfo;
import com.uiauto.service.UserService;
import com.uiauto.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    /**
     * 获取所有用户列表（用于下拉框选择）
     * @return 用户列表
     */
    @Override
    public List<User> listAllUsers() {
        return this.list(new LambdaQueryWrapper<User>()
            .orderByDesc(User::getCreateTime)
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getUserByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        String encryptedPassword = org.springframework.util.DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set("user:token:" + token, user.getId(), 7L, TimeUnit.DAYS);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:token:" + token);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        Long userId = jwtUtil.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Refresh token 无效");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String newToken = jwtUtil.generateToken(userId, user.getUsername());
        redisTemplate.opsForValue().set("user:token:" + newToken, userId, 7L, TimeUnit.DAYS);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }
}