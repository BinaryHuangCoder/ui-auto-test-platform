package com.uiauto.controller;

import com.uiauto.entity.User;
import com.uiauto.model.LoginRequest;
import com.uiauto.model.LoginResponse;
import com.uiauto.model.Result;
import com.uiauto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
                userService.logout(token);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 刷新 token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody String refreshToken) {
        try {
            if (!StringUtils.hasText(refreshToken)) {
                return Result.error("Refresh token 不能为空");
            }
            LoginResponse response = userService.refreshToken(refreshToken);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            User user = userService.getById(userId);
            if (user != null) {
                user.setPassword(null); // 不返回密码
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(HttpServletRequest request, @RequestBody User userUpdate) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 更新允许的字段
            if (userUpdate.getNickname() != null) {
                user.setNickname(userUpdate.getNickname());
            }
            if (userUpdate.getEmail() != null) {
                user.setEmail(userUpdate.getEmail());
            }
            if (userUpdate.getAvatar() != null) {
                user.setAvatar(userUpdate.getAvatar());
            }
            if (userUpdate.getGender() != null) {
                user.setGender(userUpdate.getGender());
            }
            if (userUpdate.getDepartment() != null) {
                user.setDepartment(userUpdate.getDepartment());
            }
            if (userUpdate.getPhone() != null) {
                user.setPhone(userUpdate.getPhone());
            }
            if (userUpdate.getEmployeeNo() != null) {
                user.setEmployeeNo(userUpdate.getEmployeeNo());
            }
            userService.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}
