package com.ssjheima.controller;

import com.ssjheima.pojo.Dept;
import com.ssjheima.pojo.Result;
import com.ssjheima.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping("/depts")
    public Result list(){

        List<Dept> deptList =deptService.list();
        log.info("查询全部部门信息");
        return Result.success(deptList);
    }

    @DeleteMapping("/depts/{id}")
    public Result deleteforid(@PathVariable Integer id){

        log.info("根据id删除部门信息：{}",id);
        deptService.deleteforid(id);
        return Result.success();
    }

    @PostMapping("/depts/update/{id}")
    public Result updateforid(@PathVariable Integer id){

        log.info("根据id修改部门信息：{}",id);
        deptService.updateforid(id);
        return Result.success();
    }

    @PutMapping("/depts/name/{name}")
    public Result insertforname( @PathVariable String name){

        log.info("添加一个部门：{}",name);
        deptService.insertforname(name);
        return Result.success();
    }

}
