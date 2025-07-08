package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {
    /**
     * 查询对应时间段运营数据
     * @param beginTime
     * @param endTime
     * @return
     */
    BusinessDataVO getBusinessDate(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO getSetmealsOverView();

    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO getDishesOverView();

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();
}
