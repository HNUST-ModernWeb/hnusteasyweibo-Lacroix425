package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.weibo.entity.User;
import com.weibo.mapper.UserMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/test")
    public Result test() {
        return Result.success("服务器正常");
    }

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");

        User existUser = userMapper.selectOne(
            new QueryWrapper<User>().eq("username", username)
        );
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname != null ? nickname : username);
        user.setAvatar("/uploads/default-avatar.png");
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
            );

            if (user == null) {
                return Result.error("用户不存在");
            }
            
            if (!password.equals(user.getPassword())) {
                return Result.error("密码错误");
            }

            String token = jwtUtil.generateToken(user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器错误: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result getCurrentUser(HttpServletRequest request) {
        Long userId = getUserId(request);
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
