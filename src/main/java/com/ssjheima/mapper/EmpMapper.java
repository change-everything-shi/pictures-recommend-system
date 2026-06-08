package com.ssjheima.mapper;


import com.ssjheima.pojo.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmpMapper {

    @Select("select count(*) from emp")
    public long count();

    @Select("select * from emp limit #{arg0},#{arg1}")
    public List<Emp> page(Integer start, Integer pageSize);

}
