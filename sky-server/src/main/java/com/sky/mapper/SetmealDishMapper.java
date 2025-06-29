package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 查询传入的dishId列表关联的setmealId列表
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);
}
