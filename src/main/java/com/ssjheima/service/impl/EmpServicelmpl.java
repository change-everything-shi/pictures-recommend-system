package com.ssjheima.service.impl;

import com.ssjheima.mapper.EmpMapper;
import com.ssjheima.pojo.Emp;
import com.ssjheima.pojo.PageBean;
import com.ssjheima.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EmpServicelmpl implements EmpService {


    @Resource
    @Autowired
    private EmpMapper empMapper;


    @Override
    public PageBean page(Integer page, Integer pageSize) {
        Long count = empMapper.count();
        Integer start = (page-1)*pageSize;
        List<Emp> empList = empMapper.page(start,pageSize);
        PageBean pageBean = new PageBean(count,empList);
        return pageBean;
    }
}
