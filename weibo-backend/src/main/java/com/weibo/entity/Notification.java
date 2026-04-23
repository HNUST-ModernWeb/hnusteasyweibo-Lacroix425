package com.weibo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("from_user_id")
    private Long fromUserId;
    private String type;
    @TableField("post_id")
    private Long postId;
    private String content;
    @TableField("is_read")
    private Integer isRead;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
