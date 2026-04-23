# 简易社交分享平台 - 后端说明

## 技术栈
- Java 17
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT 0.11.5

## 项目结构
```
weibo-backend/
├── src/main/java/com/weibo/
│   ├── WeiboApplication.java      # 启动类
│   ├── entity/                    # 实体类（4个）
│   ├── mapper/                    # Mapper接口（4个）
│   ├── controller/                # 控制器（5个）
│   ├── util/                      # 工具类（2个）
│   └── config/                    # 配置类（1个）
├── src/main/resources/
│   └── application.yml            # 应用配置
├── uploads/                       # 图片上传目录
└── pom.xml                        # Maven配置
```

## 数据库配置
- 数据库：weibo_platform
- 用户名：root
- 密码：Xt214998
- 端口：3306

## API接口

### 认证接口
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- GET /api/auth/me - 获取当前用户

### 动态接口
- GET /api/posts - 获取动态列表
- GET /api/posts/{id} - 获取动态详情
- POST /api/posts - 发布动态
- DELETE /api/posts/{id} - 删除动态（需登录）

### 评论接口
- GET /api/comments/post/{postId} - 获取评论列表
- POST /api/comments - 添加评论（需登录）
- DELETE /api/comments/{id} - 删除评论（需登录）

### 点赞接口
- POST /api/likes - 点赞/取消点赞（需登录）
- GET /api/likes/post/{postId} - 获取点赞状态（需登录）

### 用户接口
- GET /api/users/{id} - 获取用户信息
- PUT /api/users/{id} - 更新用户信息（需登录）
- GET /api/users/{id}/posts - 获取用户动态列表

## 启动方式

### 方式1：命令行启动
```bash
cd weibo-backend
mvn spring-boot:run
```

### 方式2：运行jar包
```bash
cd weibo-backend
mvn clean package
java -jar target/weibo-backend-1.0.0.jar
```

## 启动后访问
- API地址：http://localhost:8080
- 端口：8080

## 注意事项
1. 确保MySQL服务已启动
2. 确保数据库weibo_platform已创建
3. 确保uploads目录存在
4. 首次启动会自动创建表结构（如果表不存在）

## 测试建议
1. 先注册用户
2. 使用token访问需要认证的接口
3. 测试发布动态、评论、点赞等功能
4. 测试权限控制（删除他人动态应失败）

## 挑战级功能验证
- [x] 文字分享 - Post.content
- [x] 图片上传 - MultipartFile
- [x] 评论功能 - Comment表
- [x] 点赞功能 - Likes表 + toggle逻辑
- [x] 权限控制 - userId比对验证
