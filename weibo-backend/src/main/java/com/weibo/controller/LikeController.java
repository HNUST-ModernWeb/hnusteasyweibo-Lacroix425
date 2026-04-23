package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.weibo.entity.Likes;
import com.weibo.entity.Post;
import com.weibo.mapper.LikesMapper;
import com.weibo.mapper.PostMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikesMapper likesMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationController notificationController;

    @PostMapping
    public Result toggleLike(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long userId = getUserId(request);
            Long postId = Long.valueOf(params.get("postId").toString());

            Likes existingLike = likesMapper.selectOne(
                new QueryWrapper<Likes>()
                    .eq("user_id", userId)
                    .eq("post_id", postId)
            );

boolean liked;
        if (existingLike != null) {
            likesMapper.deleteById(existingLike.getId());
            Post post = postMapper.selectById(postId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
            liked = false;
        } else {
            Likes like = new Likes();
            like.setUserId(userId);
            like.setPostId(postId);
            likesMapper.insert(like);
            Post post = postMapper.selectById(postId);
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            liked = true;
            notificationController.createNotification(post.getUserId(), userId, "like", postId, "赞了你的动态");
        }

            HashMap<String, Object> data = new HashMap<>();
            data.put("liked", liked);
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("错误: " + e.getMessage());
        }
    }

    @GetMapping("/post/{postId}")
    public Result getLikeStatus(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = getUserId(request);

        Likes like = likesMapper.selectOne(
            new QueryWrapper<Likes>()
                .eq("user_id", userId)
                .eq("post_id", postId)
        );

        Post post = postMapper.selectById(postId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("liked", like != null);
        data.put("likeCount", post.getLikeCount());
        return Result.success(data);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
