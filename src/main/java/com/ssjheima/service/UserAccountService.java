package com.ssjheima.service;

import com.ssjheima.pojo.UserAccount;

public interface UserAccountService {
    UserAccount register(String username, String password);
    UserAccount login(String username, String password);
    UserAccount findById(Integer id);
}

