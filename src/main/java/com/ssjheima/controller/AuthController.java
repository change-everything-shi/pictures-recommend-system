package com.ssjheima.controller;

import com.ssjheima.pojo.Result;
import com.ssjheima.pojo.UserAccount;
import com.ssjheima.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    public static final String SESSION_USER_ID = "loginUserId";
    public static final String SESSION_USERNAME = "loginUsername";

    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("/register")
    public Result register(@RequestParam String username,
                           @RequestParam String password) {
        log.info("注册用户：{}", username);
        UserAccount user = userAccountService.register(username, password);
        if (user == null) {
            return Result.error("用户名已存在");
        }
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        log.info("用户登录：{}", username);
        UserAccount user = userAccountService.login(username, password);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USERNAME, user.getUsername());
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(SESSION_USER_ID);
        log.info("用户退出：{}", uid);
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/me")
    public Result me(HttpSession session) {
        Integer uid = (Integer) session.getAttribute(SESSION_USER_ID);
        String username = (String) session.getAttribute(SESSION_USERNAME);
        if (uid == null) {
            return Result.error("NOT_LOGIN");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", uid);
        data.put("username", username);
        return Result.success(data);
    }
}

