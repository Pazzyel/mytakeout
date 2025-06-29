package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);
        Long dishId = dish.getId();//主键回填

        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if (dishFlavorList != null && dishFlavorList.size() > 0) {
            dishFlavorList.forEach(dishFlavor -> {dishFlavor.setDishId(dishId);});
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> resultList = dishMapper.pageQuery(dishPageQueryDTO);
        //分页查询不展示口味信息，我们无需查询
        return new PageResult(resultList.getTotal(),resultList.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //在售的菜品不能删除
        Integer count = dishMapper.countOnSale(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //关联套餐的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //都没有异常才可以删除菜品
        dishMapper.deleteBatch(ids);
        //还要删除对应的口味
        for (Long id: ids){
            dishFlavorMapper.deleteByDishId(id);
        }
    }
}
