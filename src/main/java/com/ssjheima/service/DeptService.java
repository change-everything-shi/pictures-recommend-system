package com.ssjheima.service;

import com.ssjheima.pojo.Dept;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DeptService {
    /*
    查询全部部门信息
     */
    List<Dept> list();

    void deleteforid(Integer id);

    void updateforid(Integer id);

    void insertforname(String name);
}
