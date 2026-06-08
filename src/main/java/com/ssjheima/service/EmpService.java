package com.ssjheima.service;

import com.ssjheima.pojo.PageBean;
import org.springframework.stereotype.Service;


public interface EmpService {
    PageBean page(Integer page, Integer pageSize);
}
