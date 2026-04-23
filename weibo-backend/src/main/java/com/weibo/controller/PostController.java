package com.weibo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weibo.entity.Post;
import com.weibo.entity.User;
import com.weibo.mapper.PostMapper;
import com.weibo.mapper.UserMapper;
import com.weibo.mapper.CommentMapper;
import com.weibo.mapper.LikesMapper;
import com.weibo.util.JwtUtil;
import com.weibo.util.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikesMapper likesMapper;

    @GetMapping
    public Result getPosts(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String hashTag) {
        Page<Post> postPage = new Page<>(page, size);
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if (hashTag != null && !hashTag.isEmpty()) {
            wrapper.like("content", hashTag);
        }
        wrapper.orderByDesc("created_at");
        IPage<Post> result = postMapper.selectPage(postPage, wrapper);
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Post post : result.getRecords()) {
            User user = userMapper.selectById(post.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "用户" + post.getUserId());
            map.put("username", user != null ? user.getUsername() : "");
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createdAt", post.getCreatedAt());
            map.put("hashTag", post.getHashTag());
            data.add(map);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("records", data);
        response.put("total", result.getTotal());
        response.put("size", result.getSize());
        response.put("current", result.getCurrent());
        response.put("pages", result.getPages());
        return Result.success(response);
    }

    @GetMapping("/trending")
    public Result getTrending() {
        var posts = postMapper.selectList(
            new QueryWrapper<Post>()
                .orderByDesc("like_count")
                .last("LIMIT 10")
        );
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Post post : posts) {
            User user = userMapper.selectById(post.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "用户" + post.getUserId());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createdAt", post.getCreatedAt());
            data.add(map);
        }
        return Result.success(data);
    }

    @GetMapping("/tag/{tag}")
    public Result getByTag(@PathVariable String tag) {
        String searchTag = tag.startsWith("#") ? tag : "#" + tag;
        var posts = postMapper.selectList(
            new QueryWrapper<Post>()
                .like("content", searchTag)
                .orderByDesc("created_at")
        );
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Post post : posts) {
            User user = userMapper.selectById(post.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "用户" + post.getUserId());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createdAt", post.getCreatedAt());
            data.add(map);
        }
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result getPost(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }
        User user = userMapper.selectById(post.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("post", post);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping
    public Result createPost(HttpServletRequest request,
                            @RequestParam String content,
                            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        Long userId = getUserId(request);

        String hashTag = "";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#[\\u4e00-\\u9fa5a-zA-Z0-9]+");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            hashTag += matcher.group() + ",";
        }
        if (hashTag.endsWith(",")) hashTag = hashTag.substring(0, hashTag.length() - 1);

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setHashTag(hashTag);
        post.setLikeCount(0);
        post.setCommentCount(0);

        if (images != null && images.length > 0) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    String filename = UUID.randomUUID() + ".jpg";
                    String path = "uploads/posts/" + filename;
                    try {
                        new File("uploads/posts").mkdirs();
                        image.transferTo(new File(path));
                        imageUrls.add("/" + path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!imageUrls.isEmpty()) {
                post.setImages(String.join(",", imageUrls));
            }
        }

        postMapper.insert(post);
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("post", post);
        data.put("user", user);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result deletePost(@PathVariable Long id, HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Result.error("请先登录");
        }
        try {
            String token = auth.replace("Bearer ", "");
            Long userId = jwtUtil.getUserId(token);
            Post post = postMapper.selectById(id);
            if (post == null) {
                return Result.error("动态不存在");
            }
            if (!post.getUserId().equals(userId)) {
                return Result.error("无权限删除");
            }
            commentMapper.delete(new QueryWrapper<com.weibo.entity.Comment>().eq("post_id", id));
            likesMapper.delete(new QueryWrapper<com.weibo.entity.Likes>().eq("post_id", id));
            postMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
