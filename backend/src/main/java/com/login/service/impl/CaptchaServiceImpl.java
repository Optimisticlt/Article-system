package com.login.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.login.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    private static final long CAPTCHA_EXPIRE_MS = 5 * 60 * 1000;

    @Override
    public Map<String, String> generateCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(130, 40, 4, 80);
        String key = UUID.randomUUID().toString();
        String code = captcha.getCode();

        captchaStore.put(key, new CaptchaEntry(code, System.currentTimeMillis()));
        cleanExpired();

        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(captcha.getImageBytes());

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", base64);
        return result;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        CaptchaEntry entry = captchaStore.remove(key);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() - entry.timestamp > CAPTCHA_EXPIRE_MS) {
            return false;
        }
        return entry.code.equalsIgnoreCase(code);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(e -> now - e.getValue().timestamp > CAPTCHA_EXPIRE_MS);
    }

    private record CaptchaEntry(String code, long timestamp) {}
}
