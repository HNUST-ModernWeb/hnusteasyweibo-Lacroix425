package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weibo.entity.Post;
import com.weibo.entity.User;
import com.weibo.mapper.PostMapper;
import com.weibo.mapper.UserMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{id}")
    public Result getUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, HttpServletRequest request,
                            @RequestBody User params) {
        Long currentUserId = getUserId(request);

        if (!id.equals(currentUserId)) {
            return Result.error("无权限修改");
        }

        User user = userMapper.selectById(id);
        if (params.getNickname() != null) user.setNickname(params.getNickname());
        if (params.getAvatar() != null) user.setAvatar(params.getAvatar());
        userMapper.updateById(user);

        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/{id}/posts")
    public Result getUserPosts(@PathVariable Long id,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        IPage<Post> result = postMapper.selectPage(postPage,
            new QueryWrapper<Post>()
                .eq("user_id", id)
                .orderByDesc("created_at")
        );
        return Result.success(result);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
