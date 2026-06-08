package com.ssjheima.service.impl;

import com.ssjheima.mapper.UserAccountMapper;
import com.ssjheima.pojo.UserAccount;
import com.ssjheima.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class UserAccountServicelmpl implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Override
    public UserAccount register(String username, String password) {
        UserAccount exist = userAccountMapper.findByUsername(username);
        if (exist != null) {
            return null;
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        // 按需求：不加密，直接存明文（字段名保持 passwordHash 不改，避免动表/Mapper）
        user.setPasswordHash(password);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userAccountMapper.insert(user);
        return userAccountMapper.findByUsername(username);
    }

    @Override
    public UserAccount login(String username, String password) {
        UserAccount user = userAccountMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        String stored = user.getPasswordHash();
        // 兼容旧数据：如果库里是 64 位十六进制，按 sha256 比较；否则按明文比较
        if (stored != null && HEX_64.matcher(stored).matches()) {
            String hash = sha256Hex(password);
            if (!hash.equals(stored)) return null;
        } else {
            if (stored == null || !stored.equals(password)) return null;
        }
        return user;
    }

    @Override
    public UserAccount findById(Integer id) {
        return userAccountMapper.findById(id);
    }

    private static final Pattern HEX_64 = Pattern.compile("^[0-9a-fA-F]{64}$");

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

