package com.ssjheima.mapper;


import com.ssjheima.pojo.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeptMapper  {
    /*
    查询全部部门信息
     */
    @Select("select * from dept")
    List<Dept> list();

    @Delete("delete from emp where id = #{id}")
    void deleteforid(Integer id);

    void updateforid(Dept dept);

    void insertforname(Dept dept);
}
