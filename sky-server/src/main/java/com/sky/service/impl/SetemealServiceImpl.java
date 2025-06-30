package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetemealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO dto) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        //先保存到套餐表
        setmealMapper.insert(setmeal);//主键回填
        //关联菜品批量保存
        List<SetmealDish> setmealDishes = dto.getSetmealDishes();
        //这里还没有setmealId的信息，我们需要手动注入
        Long setmealId = setmeal.getId();
        setmealDishes.forEach(d -> d.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);
    }
}
