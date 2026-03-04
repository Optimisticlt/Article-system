SET NAMES utf8mb4;
USE user_login;

-- sys_user: 补充昵称
UPDATE sys_user SET nickname='超级管理员' WHERE id=1;
UPDATE sys_user SET nickname='张小明' WHERE id=2;
UPDATE sys_user SET nickname='李晓红' WHERE id=3;

-- ============================================================
-- article: 10 篇文章
-- ============================================================
INSERT IGNORE INTO article (id, title, content, summary, status, view_count, user_id, category_id, is_top, create_time) VALUES
(1, 'Spring Boot 快速入门指南',
'# Spring Boot 快速入门指南\n\nSpring Boot 是基于 Spring Framework 的快速开发框架，通过自动配置简化了繁琐的 XML 配置。\n\n## 创建第一个项目\n\n使用 Spring Initializr 快速生成项目骨架：\n\n1. 访问 https://start.spring.io\n2. 选择 Maven 项目、Java 语言\n3. 添加 `Spring Web`、`Spring Data JPA` 依赖\n4. 下载并导入 IDE\n\n## Hello World 示例\n\n```java\n@RestController\npublic class HelloController {\n    @GetMapping("/hello")\n    public String hello() {\n        return "Hello, Spring Boot!";\n    }\n}\n```\n\n## 自动配置原理\n\nSpring Boot 通过 `@EnableAutoConfiguration` 扫描 classpath 下的 `META-INF/spring.factories`，自动加载符合条件的配置类。\n\n## 常用注解\n\n| 注解 | 说明 |\n|------|------|\n| `@SpringBootApplication` | 启动类注解 |\n| `@RestController` | 标识 REST 控制器 |\n| `@GetMapping` | 映射 GET 请求 |\n| `@PostMapping` | 映射 POST 请求 |\n\n## 总结\n\nSpring Boot 极大降低了 Spring 项目的上手门槛，是现代 Java 后端开发的首选框架。',
'介绍 Spring Boot 的核心概念、自动配置原理和快速入门步骤，适合 Java 初学者阅读。',
1, 342, 1, 1, 1, '2026-02-10 10:00:00'),

(2, 'Vue 3 Composition API 详解',
'# Vue 3 Composition API 详解\n\nVue 3 引入了 Composition API，解决了 Vue 2 Options API 在大型项目中逻辑分散的问题。\n\n## 为什么需要 Composition API\n\n在 Options API 中，同一功能的逻辑分散在 `data`、`methods`、`computed`、`watch` 等不同选项中，难以复用。\n\n## 基础用法\n\n```javascript\nimport { ref, reactive, computed, onMounted } from "vue"\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const user = reactive({ name: "Alice", age: 18 })\n    const double = computed(() => count.value * 2)\n    onMounted(() => console.log("组件已挂载"))\n    return { count, user, double }\n  }\n}\n```\n\n## script setup 语法糖\n\n```vue\n<script setup>\nimport { ref } from "vue"\nconst count = ref(0)\n</script>\n```\n\n## 自定义 Hook\n\n```javascript\nexport function useFetch(url) {\n  const data = ref(null)\n  const loading = ref(true)\n  fetch(url).then(r => r.json()).then(d => { data.value = d; loading.value = false })\n  return { data, loading }\n}\n```\n\n## 总结\n\nComposition API 让代码组织更灵活，逻辑复用更方便，是 Vue 3 最重要的新特性之一。',
'深入讲解 Vue 3 Composition API 的设计理念、基础用法和自定义 Hook 的最佳实践。',
1, 278, 1, 1, 0, '2026-02-12 14:30:00'),

(3, 'MySQL 索引优化实践',
'# MySQL 索引优化实践\n\n索引是 MySQL 性能优化的核心手段，合理使用索引可以将查询速度提升数十倍。\n\n## 索引类型\n\n- **B+Tree 索引**：最常用，支持范围查询\n- **Hash 索引**：等值查询极快，不支持范围\n- **全文索引**：用于文本搜索\n\n## 索引失效场景\n\n```sql\n-- 对索引列使用函数（失效）\nSELECT * FROM user WHERE YEAR(create_time) = 2026;\n-- 改写为范围查询（有效）\nSELECT * FROM user WHERE create_time >= "2026-01-01";\n\n-- LIKE 前缀通配符（失效）\nSELECT * FROM user WHERE name LIKE "%张%";\n-- 改为右侧通配（有效）\nSELECT * FROM user WHERE name LIKE "张%";\n```\n\n## EXPLAIN 分析\n\n```sql\nEXPLAIN SELECT * FROM article WHERE user_id = 1;\n```\n\n关注 `type` 列：`system > const > eq_ref > ref > range > index > ALL`\n\n## 联合索引最左前缀\n\n```sql\n-- 联合索引 (a, b, c)\nSELECT * FROM t WHERE a=1;           -- 有效\nSELECT * FROM t WHERE a=1 AND b=2;   -- 有效\nSELECT * FROM t WHERE b=2;           -- 无效，跳过了 a\n```\n\n## 总结\n\n索引不是越多越好，每个额外索引都会影响写入性能。建议根据实际查询场景按需创建索引。',
'总结 MySQL 索引的类型、失效场景、EXPLAIN 用法和联合索引最左前缀原则，附有实际 SQL 示例。',
1, 195, 2, 2, 0, '2026-02-15 09:00:00'),

(4, 'MyBatis-Plus 使用教程',
'# MyBatis-Plus 使用教程\n\nMyBatis-Plus 是 MyBatis 的增强工具，在不改变原有功能的基础上提供了大量便捷操作。\n\n## 快速开始\n\n```xml\n<dependency>\n    <groupId>com.baomidou</groupId>\n    <artifactId>mybatis-plus-boot-starter</artifactId>\n    <version>3.5.5</version>\n</dependency>\n```\n\n## 基础 CRUD\n\n```java\n@Mapper\npublic interface UserMapper extends BaseMapper<SysUser> {}\n\n// 条件查询\nLambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();\nwrapper.eq(SysUser::getStatus, 1).orderByDesc(SysUser::getCreateTime);\nList<SysUser> users = userMapper.selectList(wrapper);\n\n// 分页\nPage<SysUser> page = new Page<>(1, 10);\nuserMapper.selectPage(page, wrapper);\n```\n\n## 逻辑删除配置\n\n```yaml\nmybatis-plus:\n  global-config:\n    db-config:\n      logic-delete-field: isDeleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n```\n\n## 自动填充\n\n```java\n@Component\npublic class MyMetaObjectHandler implements MetaObjectHandler {\n    @Override\n    public void insertFill(MetaObject metaObject) {\n        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());\n    }\n}\n```\n\n## 总结\n\nMyBatis-Plus 大幅减少了重复的 CRUD 代码，是 Spring Boot 项目持久层的优秀选择。',
'介绍 MyBatis-Plus 的快速接入、Lambda 条件构造器、分页插件和逻辑删除等核心功能。',
1, 156, 2, 2, 0, '2026-02-18 11:00:00'),

(5, 'Spring Security + JWT 认证方案实战',
'# Spring Security + JWT 认证方案实战\n\n本文介绍如何在 Spring Boot 项目中集成 Spring Security 和 JWT，实现无状态的 RESTful API 认证。\n\n## 整体流程\n\n```\n用户登录 → 服务端验证 → 签发 JWT → 客户端存储\n后续请求 → 携带 JWT → 服务端验证 → 返回数据\n```\n\n## JWT 结构\n\nJWT 由三部分组成：Header.Payload.Signature\n\nPayload 示例：\n```json\n{\n  "sub": "用户ID",\n  "userName": "admin",\n  "role": "admin",\n  "type": "access",\n  "exp": 1700001800\n}\n```\n\n## 核心配置\n\n```java\n@Bean\npublic SecurityFilterChain filterChain(HttpSecurity http) throws Exception {\n    http\n        .csrf(AbstractHttpConfigurer::disable)\n        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))\n        .authorizeHttpRequests(auth -> auth\n            .requestMatchers("/api/auth/**").permitAll()\n            .requestMatchers("/api/admin/**").hasRole("ADMIN")\n            .anyRequest().authenticated()\n        )\n        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);\n    return http.build();\n}\n```\n\n## Token 刷新机制\n\n- **Access Token**：有效期 30 分钟，用于 API 请求\n- **Refresh Token**：有效期 7 天，用于刷新 Access Token\n\n## 总结\n\nJWT 无状态认证方案适合前后端分离项目，配合 Token 刷新机制可以兼顾安全性和用户体验。',
'详解 Spring Security 与 JWT 的整合方案，包括过滤器配置、Token 生成验证和双 Token 刷新机制。',
1, 421, 1, 1, 0, '2026-02-20 16:00:00'),

(6, 'Vue + Spring Boot 前后端分离项目实战',
'# Vue + Spring Boot 前后端分离项目实战\n\n本文记录了使用 Vue 3 + Spring Boot 开发个人博客系统的完整过程。\n\n## 技术选型\n\n| 层次 | 技术栈 |\n|------|--------|\n| 前端 | Vue 3 + Vite + Element Plus + Axios |\n| 后端 | Spring Boot 3 + Spring Security + MyBatis-Plus |\n| 数据库 | MySQL 8.0 |\n| 认证 | JWT 双 Token 机制 |\n\n## 项目结构\n\n```\n前端 (5173端口)\n  src/\n  ├── api/         请求封装\n  ├── views/       页面组件\n  ├── layout/      布局组件\n  ├── router/      路由配置\n  └── utils/       工具函数\n\n后端 (8080端口)\n  src/main/java/\n  ├── controller/  接口层\n  ├── service/     业务层\n  ├── mapper/      数据访问层\n  └── config/      配置类\n```\n\n## 踩坑记录\n\n1. 403 问题：登录后未保存 userRole 到 localStorage，导致路由守卫拦截\n2. 中文乱码：MySQL 客户端字符集为 GBK，需在 SQL 开头加 SET NAMES utf8mb4\n3. Token 刷新：并发请求时需加锁防止多次刷新\n\n## 总结\n\n前后端分离架构提升了开发效率和系统可维护性，推荐在中大型项目中采用。',
'记录 Vue 3 + Spring Boot 博客系统的完整开发过程，包括技术选型、项目结构设计和常见问题解决方案。',
1, 389, 1, 3, 0, '2026-02-22 10:30:00'),

(7, '用 Spring Boot 实现文件上传功能',
'# 用 Spring Boot 实现文件上传功能\n\n文件上传是 Web 应用中的常见需求，本文介绍 Spring Boot 中文件上传的最佳实践。\n\n## 配置文件大小限制\n\n```yaml\nspring:\n  servlet:\n    multipart:\n      max-file-size: 5MB\n      max-request-size: 10MB\n```\n\n## 控制器代码\n\n```java\n@PostMapping("/upload")\npublic Result upload(@RequestParam("file") MultipartFile file) {\n    if (file.isEmpty()) return Result.error("文件为空");\n    String filename = UUID.randomUUID() + getExtension(file);\n    Path path = Paths.get(uploadPath, filename);\n    Files.copy(file.getInputStream(), path);\n    return Result.success("/uploads/" + filename);\n}\n```\n\n## 访问上传文件\n\n```java\n@Override\npublic void addResourceHandlers(ResourceHandlerRegistry registry) {\n    registry.addResourceHandler("/uploads/**")\n            .addResourceLocations("file:./uploads/");\n}\n```\n\n## 文件类型校验\n\n```java\nprivate static final Set<String> ALLOWED = Set.of("jpg","jpeg","png","gif","webp");\nString ext = FilenameUtils.getExtension(filename).toLowerCase();\nif (!ALLOWED.contains(ext)) throw new BusinessException("不支持的文件格式");\n```\n\n## 总结\n\n文件上传需注意大小限制、类型校验和存储路径安全，生产环境建议使用 OSS 存储。',
'介绍 Spring Boot 文件上传的完整实现，包括配置、控制器代码、静态资源访问和文件类型校验。',
1, 134, 3, 3, 0, '2026-02-25 15:00:00'),

(8, 'JavaScript 异步编程：从回调到 async/await',
'# JavaScript 异步编程：从回调到 async/await\n\nJavaScript 的异步编程经历了三个阶段：回调函数 → Promise → async/await。\n\n## 回调地狱\n\n```javascript\ngetUser(id, function(user) {\n    getOrders(user.id, function(orders) {\n        getDetail(orders[0].id, function(detail) {\n            // 嵌套越来越深...\n        })\n    })\n})\n```\n\n## Promise 改进\n\n```javascript\ngetUser(id)\n  .then(user => getOrders(user.id))\n  .then(orders => getDetail(orders[0].id))\n  .then(detail => console.log(detail))\n  .catch(err => console.error(err))\n```\n\n## async/await 最佳实践\n\n```javascript\nasync function loadUserDetail(id) {\n  try {\n    const user = await getUser(id)\n    const orders = await getOrders(user.id)\n    const detail = await getDetail(orders[0].id)\n    return detail\n  } catch (err) {\n    console.error("加载失败:", err)\n  }\n}\n```\n\n## 并行执行\n\n```javascript\n// 串行（慢）\nconst a = await fetchA()\nconst b = await fetchB()\n\n// 并行（快）\nconst [a, b] = await Promise.all([fetchA(), fetchB()])\n```\n\n## 总结\n\nasync/await 让异步代码如同步代码一样可读，配合 Promise.all 处理并发是现代前端开发标准做法。',
'系统梳理 JavaScript 异步编程的演进历史，对比回调、Promise 和 async/await 的优缺点，附实用代码示例。',
1, 267, 1, 1, 0, '2026-02-28 09:00:00'),

(9, '程序员如何高效学习新技术',
'# 程序员如何高效学习新技术\n\n技术更新迭代越来越快，如何在有限时间内高效掌握新技术是每个程序员都需要思考的问题。\n\n## 学习方法论\n\n### 先了解全貌，再深入细节\n\n不要一开始就啃官方文档的每一个细节。先花 30 分钟看官方 Getting Started，跑起来一个 Demo，建立整体认知。\n\n### 动手实践是最好的老师\n\n看再多的教程不如自己写一遍代码。建议边学边做一个小项目，将知识点串联起来。\n\n### 费曼学习法\n\n能把一个技术讲清楚给别人听，才算真正掌握了。写博客、做分享是巩固知识的最佳方式。\n\n## 推荐学习路线\n\n```\n官方文档（Quick Start）→ 视频教程 → 动手做项目 → 阅读源码 → 输出总结\n```\n\n## 避免的误区\n\n- 收藏了一堆教程但从不看\n- 只看不练，看完就忘\n- 追求完美，不敢开始\n\n## 总结\n\n持续输入 + 刻意练习 + 适时输出，是程序员保持成长的不二法门。',
'分享程序员高效学习新技术的方法论，包括费曼学习法、实践驱动和避免的常见误区。',
1, 198, 2, 4, 0, '2026-03-01 10:00:00'),

(10, '我的 2025 年度总结',
'# 我的 2025 年度总结\n\n转眼又是一年，回顾这一年走过的路，记录下来，也给自己一个交代。\n\n## 技术成长\n\n- 系统学习了 Spring Boot 全家桶，做了两个完整项目\n- 从零掌握了 Vue 3 + TypeScript，告别 Options API\n- 深入理解了 MySQL 索引和查询优化\n- 第一次独立部署了线上服务（Linux + Nginx + Docker）\n\n## 阅读书单\n\n1. 《深入理解 Java 虚拟机》— 周志明\n2. 《代码整洁之道》— Robert C. Martin\n3. 《凤凰项目》— 轻松理解 DevOps 文化\n\n## 习惯养成\n\n坚持每天写代码，哪怕只有 30 分钟。坚持写技术博客，倒逼自己思考和总结。\n\n## 2026 年计划\n\n- 深入学习分布式系统\n- 刷完 LeetCode 200 道题\n- 读 5 本技术书\n- 保持健身习惯\n\n## 写在最后\n\n路漫漫其修远兮，继续努力。\n\n> 种一棵树最好的时间是十年前，其次是现在。',
'2025 年度个人技术成长总结，记录技术学习、阅读书单和新一年的计划。',
1, 312, 3, 4, 0, '2026-03-02 20:00:00');

-- ============================================================
-- article_tag: 文章标签关联
-- ============================================================
INSERT IGNORE INTO article_tag (article_id, tag_id) VALUES
(1, 1), (1, 2), (1, 7),
(2, 3), (2, 5), (2, 6),
(3, 4), (3, 7), (3, 8),
(4, 1), (4, 4), (4, 7),
(5, 1), (5, 2), (5, 7),
(6, 2), (6, 3), (6, 6), (6, 7),
(7, 1), (7, 2), (7, 7),
(8, 5), (8, 6),
(10, 1);

-- ============================================================
-- comment: 每篇文章若干评论，含嵌套回复
-- ============================================================
INSERT IGNORE INTO comment (id, content, article_id, user_id, parent_id, create_time) VALUES
(1,  '写得很详细，Spring Boot 入门必读！', 1, 2, NULL, '2026-02-10 12:00:00'),
(2,  '自动配置那一块能再展开讲讲吗？', 1, 3, NULL, '2026-02-10 14:00:00'),
(3,  '感谢反馈，后续会出一篇自动配置原理深度解析。', 1, 1, 2, '2026-02-10 15:00:00'),
(4,  '跟着教程做成功了，感谢作者！', 1, 2, NULL, '2026-02-11 09:00:00'),
(5,  'Composition API 确实比 Options API 清晰多了。', 2, 2, NULL, '2026-02-12 16:00:00'),
(6,  '自定义 Hook 那个例子非常实用！', 2, 3, NULL, '2026-02-13 10:00:00'),
(7,  '请问 script setup 和 setup() 函数有什么区别？', 2, 2, NULL, '2026-02-13 11:00:00'),
(8,  'script setup 是语法糖，编译后等价，但写法更简洁，推荐使用。', 2, 1, 7, '2026-02-13 12:00:00'),
(9,  'EXPLAIN 分析那部分讲得很到位！', 3, 1, NULL, '2026-02-15 10:00:00'),
(10, '联合索引最左前缀原则终于搞明白了，之前一直搞不清楚。', 3, 3, NULL, '2026-02-15 11:00:00'),
(11, '请问覆盖索引和回表是什么关系？', 3, 2, NULL, '2026-02-16 09:00:00'),
(12, '双 Token 方案实现得很优雅！', 5, 2, NULL, '2026-02-20 18:00:00'),
(13, 'Refresh Token 需要存数据库吗？', 5, 3, NULL, '2026-02-21 09:00:00'),
(14, '可以存也可以不存。存数据库可主动吊销；不存则完全无状态，简单但无法主动失效。', 5, 1, 13, '2026-02-21 10:00:00'),
(15, '收藏了，搭项目直接用这套方案！', 5, 2, NULL, '2026-02-21 11:00:00'),
(16, '和我的项目架构几乎一模一样，踩坑记录太真实了哈哈。', 6, 3, NULL, '2026-02-22 12:00:00'),
(17, '403 那个坑我也踩过，当时排查了好久...', 6, 2, NULL, '2026-02-22 14:00:00'),
(18, '前后端分离就是香，部署也方便。', 6, 3, NULL, '2026-02-23 09:00:00'),
(19, '终于搞清楚 Promise 和 async/await 的关系了！', 8, 2, NULL, '2026-02-28 11:00:00'),
(20, 'Promise.all 并行那个例子很实用，之前都是串行等待。', 8, 3, NULL, '2026-02-28 14:00:00'),
(21, '费曼学习法 + 写博客，确实是最好的学习方式！', 9, 1, NULL, '2026-03-01 12:00:00'),
(22, '收藏了，转发给正在学习的朋友。', 9, 3, NULL, '2026-03-01 15:00:00'),
(23, '年度总结写得很真实，很有共鸣！', 10, 1, NULL, '2026-03-02 21:00:00'),
(24, '书单记下来了，《代码整洁之道》已经在读了。', 10, 2, NULL, '2026-03-03 09:00:00'),
(25, '2026 继续加油！', 10, 1, NULL, '2026-03-03 10:00:00');

-- ============================================================
-- article_like: 点赞记录
-- ============================================================
INSERT IGNORE INTO article_like (id, article_id, user_id, create_time) VALUES
(1,  1, 2, '2026-02-10 12:30:00'),
(2,  1, 3, '2026-02-10 13:00:00'),
(3,  2, 2, '2026-02-12 15:00:00'),
(4,  2, 3, '2026-02-12 16:00:00'),
(5,  3, 1, '2026-02-15 11:00:00'),
(6,  3, 3, '2026-02-15 12:00:00'),
(7,  4, 1, '2026-02-18 12:00:00'),
(8,  5, 2, '2026-02-20 18:00:00'),
(9,  5, 3, '2026-02-21 09:00:00'),
(10, 6, 2, '2026-02-22 12:00:00'),
(11, 6, 3, '2026-02-22 13:00:00'),
(12, 7, 1, '2026-02-25 16:00:00'),
(13, 7, 2, '2026-02-25 17:00:00'),
(14, 8, 2, '2026-02-28 11:00:00'),
(15, 8, 3, '2026-02-28 12:00:00'),
(16, 9, 1, '2026-03-01 11:00:00'),
(17, 9, 3, '2026-03-01 14:00:00'),
(18, 10, 1, '2026-03-02 21:00:00'),
(19, 10, 2, '2026-03-03 09:00:00'),
(20, 10, 3, '2026-03-03 10:00:00');
