package com.ssjheima.mapper;

import com.ssjheima.pojo.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAccountMapper {

    @Select("select * from user_account where username = #{username} limit 1")
    UserAccount findByUsername(String username);

    @Select("select * from user_account where id = #{id} limit 1")
    UserAccount findById(Integer id);

    void insert(UserAccount userAccount);
}

