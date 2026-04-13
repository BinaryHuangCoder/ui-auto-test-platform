package com.uiauto.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uiauto.entity.User;
import com.uiauto.model.Result;
import com.uiauto.model.LoginRequest;
import com.uiauto.model.LoginResponse;
import org.springframework.validation.annotation.Validated;
import com.uiauto.service.UserService;
import com.uiauto.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户管理控制器
 * 提供用户的增删改查接口
 * 
 * @author huangzhiyong081439
 * @date 2026-04-07
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 分页获取用户列表
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认15
     * @return Result<Page<User>> 分页用户列表
     */
    @GetMapping("/list")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userService.page(page, 
            new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime)
        );
        // 脱敏处理，不返回密码
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(result);
    }
    
    /**
     * 添加用户
     * @param user 用户信息
     * @return Result<String> 操作结果
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        // 检查用户名是否已存在
        User existUser = userService.getUserByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 加密密码
        String encryptedPassword = org.springframework.util.DigestUtils.md5DigestAsHex(
            user.getPassword() != null ? user.getPassword().getBytes() : "123456".getBytes()
        );
        user.setPassword(encryptedPassword);
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        
        boolean success = userService.save(user);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }
    
    /**
     * 更新用户
     * @param user 用户信息
     * @return Result<String> 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        // 只更新允许修改的字段
        existUser.setNickname(user.getNickname());
        existUser.setEmail(user.getEmail());
        if (user.getStatus() != null) {
            existUser.setStatus(user.getStatus());
        }
        existUser.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(existUser);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return Result<String> 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        if (id == null) {
            return Result.error("用户ID不能为空");
        }
        
        User existUser = userService.getById(id);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        // 不允许删除超级管理员
        if ("admin".equals(existUser.getUsername())) {
            return Result.error("不能删除超级管理员");
        }
        
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 重置用户密码
     * @param params 包含username的请求参数
     * @return Result<String> 操作结果
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }
        
        // 重置密码为 123456
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 使用 MD5 加密密码（与登录一致）
        String encryptedPassword = org.springframework.util.DigestUtils.md5DigestAsHex("123456".getBytes());
        user.setPassword(encryptedPassword);
        userService.updateById(user);
        
        return Result.success("密码重置成功，新密码为：123456");
    }
    
    /**
     * 用户登录接口
     * @param request 登录请求，包含用户名和密码
     * @return Result<LoginResponse> 登录结果，包含token和用户信息
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
     * 获取当前登录用户信息
     * @param request HTTP请求，用于获取token
     * @return Result<User> 当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.error(401, "未登录或 token 无效");
        }
        
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error(401, "token 无效");
            }
            
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }
            
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "token 验证失败");
        }
    }
    
    /**
     * 获取所有用户列表（用于下拉框选择）
     * @return 用户列表（脱敏处理，不返回密码）
     */
    @GetMapping("/all")
    public Result<List<User>> listAllUsers() {
        List<User> users = userService.listAllUsers();
        // 脱敏处理，不返回密码
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }
    
    /**
     * 修改密码（带强度验证）
     * @param params 包含username、oldPassword、newPassword的请求参数
     * @return Result<String> 操作结果
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (username == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        // 验证密码强度：长度至少8位，必须包含大小写字母+数字+特殊字符
        if (!isPasswordStrong(newPassword)) {
            return Result.error("密码强度不足！密码必须满足：\n1. 长度至少8位\n2. 包含大写字母\n3. 包含小写字母\n4. 包含数字\n5. 包含特殊字符(!@#$%^&*)");
        }
        
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        String encryptedOld = org.springframework.util.DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!encryptedOld.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        String encryptedNew = org.springframework.util.DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(encryptedNew);
        userService.updateById(user);
        
        return Result.success("密码修改成功");
    }
    
    /**
     * 验证密码强度
     * @param password 待验证的密码
     * @return 密码强度是否符合要求
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // 包含大写字母
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }
        // 包含小写字母
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }
        // 包含数字
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return false;
        }
        // 包含特殊字符
        if (!Pattern.compile("[!@#$%^&*()_+\\-\\[\\];:'\"\\|,.<>\\/?]").matcher(password).find()) {
            return false;
        }
        return true;
    }
}