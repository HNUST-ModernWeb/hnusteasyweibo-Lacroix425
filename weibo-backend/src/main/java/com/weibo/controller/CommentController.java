package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.weibo.entity.Comment;
import com.weibo.entity.Post;
import com.weibo.entity.User;
import com.weibo.mapper.CommentMapper;
import com.weibo.mapper.PostMapper;
import com.weibo.mapper.UserMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationController notificationController;

    @GetMapping("/post/{postId}")
    public Result getComments(@PathVariable Long postId) {
        var comments = commentMapper.selectList(
            new QueryWrapper<Comment>()
                .eq("post_id", postId)
                .orderByAsc("created_at")
        );
        for (Comment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());
            comment.setUserId(user.getId());
        }
        return Result.success(comments);
    }

    @PostMapping
    public Result addComment(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = getUserId(request);
        Long postId = Long.valueOf(params.get("postId").toString());
        String content = params.get("content").toString();

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentMapper.insert(comment);

        Post post = postMapper.selectById(postId);
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        notificationController.createNotification(post.getUserId(), userId, "comment", postId, "评论了你的动态");

        User user = userMapper.selectById(userId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("comment", comment);
        data.put("user", user);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        Comment comment = commentMapper.selectById(id);

        if (!comment.getUserId().equals(userId)) {
            return Result.error("无权限删除");
        }

        commentMapper.deleteById(id);

        Post post = postMapper.selectById(comment.getPostId());
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postMapper.updateById(post);

        return Result.success();
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
