package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.weibo.entity.Notification;
import com.weibo.entity.Post;
import com.weibo.entity.User;
import com.weibo.mapper.NotificationMapper;
import com.weibo.mapper.PostMapper;
import com.weibo.mapper.UserMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result getNotifications(HttpServletRequest request) {
        Long userId = getUserId(request);
        var notifications = notificationMapper.selectList(
            new QueryWrapper<Notification>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .last("LIMIT 20")
        );
        return Result.success(notifications);
    }

    @GetMapping("/unread")
    public Result getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        Long count = notificationMapper.selectCount(
            new QueryWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", 0)
        );
        return Result.success(count);
    }

    @PostMapping("/read")
    public Result markAsRead(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            notificationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Notification>()
                    .eq("user_id", userId)
                    .set("is_read", 1)
            );
            return Result.success();
        } catch (Exception e) {
            return Result.success();
        }
    }

    public void createNotification(Long userId, Long fromUserId, String type, Long postId, String content) {
        if (userId.equals(fromUserId)) return;
        User fromUser = userMapper.selectById(fromUserId);
        String fromName = fromUser != null ? fromUser.getNickname() : "用户" + fromUserId;
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setPostId(postId);
        notification.setContent(fromName + " " + content);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}