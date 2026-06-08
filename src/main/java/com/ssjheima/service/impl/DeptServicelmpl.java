package com.ssjheima.service.impl;

import com.ssjheima.mapper.DeptMapper;
import com.ssjheima.pojo.Dept;
import com.ssjheima.pojo.Emp;
import com.ssjheima.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeptServicelmpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public List<Dept> list() {
        return deptMapper.list();
    }

    @Override
    public void deleteforid(Integer id) {
        deptMapper.deleteforid(id);
    }

    @Override
    public void updateforid(Integer id) {
        Dept dept = new Dept();
        dept.setId(id);
        dept.setName("技术部");
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateforid(dept);
    }

    @Override
    public void insertforname(String name) {
        Dept dept = new Dept();
        dept.setName(name);
        dept.setUpdateTime(LocalDateTime.now());
        dept.setCreateTime(LocalDateTime.now());
        deptMapper.insertforname(dept);
    }

}
