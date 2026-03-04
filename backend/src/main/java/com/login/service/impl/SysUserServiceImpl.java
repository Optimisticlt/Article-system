package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.*;
import com.login.entity.*;
import com.login.mapper.*;
import com.login.service.CaptchaService;
import com.login.service.SysUserService;
import com.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final ArticleLikeMapper articleLikeMapper;

    @Override
    public Result<Map<String, String>> login(LoginRequest request) {
        if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha()))
            return Result.error(400, "Invalid or expired captcha");
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, request.getUserName()));
        if (user == null || !passwordEncoder.matches(request.getPassWord(), user.getPassWord()))
            return Result.error(401, "Incorrect username or password");
        if (user.getStatus() == 0) return Result.error(403, "Account has been disabled");
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getRole()));
        tokens.put("role", user.getRole());
        if (Boolean.TRUE.equals(request.getRememberMe()))
            tokens.put("refreshToken", jwtUtil.generateRefreshToken(user.getId(), user.getUserName()));
        return Result.success(tokens);
    }

    @Override
    public Result<Void> register(RegisterRequest request) {
        if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha()))
            return Result.error(400, "Invalid or expired captcha");
        long nameCount = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, request.getUserName()));
        if (nameCount > 0) return Result.error(400, "Username already exists");
        long emailCount = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, request.getEmail()));
        if (emailCount > 0) return Result.error(400, "Email already registered");
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setPassWord(passwordEncoder.encode(request.getPassWord()));
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus(1);
        user.setIsDeleted(0);
        save(user);
        return Result.success();
    }

    @Override
    public Result<Void> forgotPassword(String email) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (user == null) return Result.success();
        log.debug("Simulating password reset email to: " + email);
        return Result.success();
    }

    @Override
    public Result<Map<String, String>> refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) return Result.error(401, "Token expired, please login again");
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) return Result.error(400, "Invalid token type");
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        SysUser user = getById(userId);
        if (user == null || user.getStatus() == 0) return Result.error(401, "User not found or disabled");
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getRole()));
        tokens.put("refreshToken", jwtUtil.generateRefreshToken(user.getId(), user.getUserName()));
        tokens.put("role", user.getRole());
        return Result.success(tokens);
    }

    @Override
    public Result<UserProfileVO> getUserProfile(Long userId) {
        SysUser user = getById(userId);
        if (user == null) return Result.error(404, "User not found");
        UserProfileVO vo = toProfileVO(user);
        long articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId).eq(Article::getStatus, 1));
        vo.setArticleCount((int) articleCount);
        long likeCount = articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                .inSql(ArticleLike::getArticleId,
                        "SELECT id FROM article WHERE user_id=" + userId + " AND is_deleted=0"));
        vo.setLikeCount((int) likeCount);
        return Result.success(vo);
    }

    @Override
    public Result<Void> updateProfile(Long userId, String nickname, String email) {
        SysUser user = getById(userId);
        if (user == null) return Result.error(404, "User not found");
        if (nickname != null) user.setNickname(nickname);
        if (email != null) {
            long count = count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email).ne(SysUser::getId, userId));
            if (count > 0) return Result.error(400, "Email already in use");
            user.setEmail(email);
        }
        updateById(user);
        return Result.success();
    }

    @Override
    public Result<Void> updatePassword(Long userId, String oldPwd, String newPwd) {
        SysUser user = getById(userId);
        if (user == null) return Result.error(404, "User not found");
        if (!passwordEncoder.matches(oldPwd, user.getPassWord()))
            return Result.error(400, "Current password is incorrect");
        user.setPassWord(passwordEncoder.encode(newPwd));
        updateById(user);
        return Result.success();
    }

    @Override
    public Result<Void> updateAvatar(Long userId, String avatarUrl) {
        SysUser user = getById(userId);
        if (user == null) return Result.error(404, "User not found");
        user.setAvatar(avatarUrl);
        updateById(user);
        return Result.success();
    }

    @Override
    public Result<PageResult<UserProfileVO>> listUsers(int page, int size) {
        Page<SysUser> pageResult = this.page(new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime));
        List<UserProfileVO> vos = pageResult.getRecords().stream()
                .map(this::toProfileVO).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    @Override
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) return Result.error(404, "User not found");
        user.setStatus(status);
        updateById(user);
        return Result.success();
    }

    @Override
    public Result<DashboardVO> getDashboardStats() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalArticles(articleMapper.selectCount(new LambdaQueryWrapper<Article>()));
        vo.setPublishedArticles(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)));
        vo.setTotalComments(commentMapper.selectCount(new LambdaQueryWrapper<Comment>()));

        List<Map<String, Object>> viewRes = articleMapper.selectMaps(
                new QueryWrapper<Article>().select("IFNULL(SUM(view_count), 0) AS total"));
        vo.setTotalViews(viewRes.isEmpty() ? 0L : ((Number) viewRes.get(0).get("total")).longValue());

        List<Map<String, Object>> catStats = articleMapper.selectMaps(
                new QueryWrapper<Article>().select("category_id, COUNT(*) AS count").groupBy("category_id"));
        catStats.forEach(stat -> {
            Object catIdObj = stat.get("category_id");
            if (catIdObj != null) {
                Category cat = categoryMapper.selectById(((Number) catIdObj).longValue());
                stat.put("categoryName", cat != null ? cat.getName() : "Unknown");
            } else {
                stat.put("categoryName", "Uncategorized");
            }
        });
        vo.setCategoryStats(catStats);

        List<Map<String, Object>> trend = articleMapper.selectMaps(
                new QueryWrapper<Article>()
                        .select("DATE(create_time) AS date, COUNT(*) AS count")
                        .ge("create_time", LocalDateTime.now().minusDays(7))
                        .groupBy("DATE(create_time)")
                        .orderByAsc("DATE(create_time)"));
        vo.setRecentTrend(trend);

        List<Article> recentList = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getCreateTime)
                        .last("LIMIT 5"));
        List<ArticleVO> recentVOs = recentList.stream().map(a -> {
            ArticleVO avo = new ArticleVO();
            avo.setId(a.getId());
            avo.setTitle(a.getTitle());
            avo.setStatus(a.getStatus());
            avo.setViewCount(a.getViewCount());
            avo.setCreateTime(a.getCreateTime());
            return avo;
        }).collect(Collectors.toList());
        vo.setRecentArticles(recentVOs);

        return Result.success(vo);
    }

    private UserProfileVO toProfileVO(SysUser user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
