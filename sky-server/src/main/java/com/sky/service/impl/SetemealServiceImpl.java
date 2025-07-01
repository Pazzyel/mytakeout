package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
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

    /**
     * 套餐分页查询
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(),queryDTO.getPageSize());
        //分页查询的SetmealVO不需要关联菜品信息
        Page<SetmealVO> page = setmealMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        //在售的套餐不能删除
        Integer count = setmealMapper.countOnSale(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //套餐菜品关系表是由套餐相关API建立的，因此删除套餐也要删除对于套餐菜品关系
        setmealMapper.deleteBatch(ids);
        for (Long id : ids) {
            setmealDishMapper.deleteBySetmealId(id);
        }
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDishes(Long id) {
        //查询套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, vo);
        //查询关联菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        vo.setSetmealDishes(setmealDishes);
        return vo;
    }

    /**
     * 修改套餐
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithDishes(SetmealDTO dto) {
        Long setmealId = dto.getId();
        //删除对应的套餐菜品关系
        setmealDishMapper.deleteBySetmealId(setmealId);
        //更新套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmealMapper.update(setmeal);
        //重新插入新的套餐菜品关系
        List<SetmealDish> setmealDishes = dto.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(d -> d.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 根据条件查询套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
