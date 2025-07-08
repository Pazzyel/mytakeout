package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询对应时间段运营数据
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public BusinessDataVO getBusinessDate(LocalDateTime beginTime, LocalDateTime endTime) {
        Map<String,Object> map = new HashMap<>();
        map.put("beginTime",beginTime);
        map.put("endTime",endTime);
        Integer totalOrderCount = orderMapper.countStatusByMap(map);

        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.sumAmountByMap(map);
        turnover = turnover == null ? 0 : turnover;
        Double orderCompletionRate = 0.0;
        Double averagePrice = 0.0;
        Integer validOrderCount = orderMapper.countStatusByMap(map);
        if(totalOrderCount != 0 && validOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;//订单完成率
            averagePrice = turnover / totalOrderCount;//平均单价
        }
        averagePrice = Math.round(averagePrice * 100.0) / 100.0;//四舍五入保留两位小数

        Integer newUser = userMapper.countUserByMap(map);//新增用户数
        return BusinessDataVO.builder().newUsers(newUser).orderCompletionRate(orderCompletionRate)
                .turnover(turnover).unitPrice(averagePrice).validOrderCount(validOrderCount).build();
    }

    /**
     * 查询套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO getSetmealsOverView() {
        Map<String,Object> map = new HashMap<>();
        map.put("status", 1);//0停售，1起售
        Integer startCount = setmealMapper.countByMap(map);
        map.put("status", 0);
        Integer stopCount = setmealMapper.countByMap(map);
        return SetmealOverViewVO.builder().discontinued(stopCount).sold(startCount).build();
    }

    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO getDishesOverView() {
        Map<String,Object> map = new HashMap<>();
        map.put("status", 1);//0停售，1起售
        Integer startCount = dishMapper.countByMap(map);
        map.put("status", 0);
        Integer stopCount = dishMapper.countByMap(map);
        return DishOverViewVO.builder().discontinued(stopCount).sold(startCount).build();
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        Map<String,Object> map = new HashMap<>();
        Integer allOrders = orderMapper.countStatusByMap(map);
        allOrders = allOrders == null ? 0 : allOrders;
        map.put("status", Orders.CANCELLED);//已取消
        Integer cancelledOrders = orderMapper.countStatusByMap(map);
        cancelledOrders = cancelledOrders == null ? 0 : cancelledOrders;
        map.put("status", Orders.COMPLETED);//已完成
        Integer completedOrders = orderMapper.countStatusByMap(map);
        completedOrders = completedOrders == null ? 0 : completedOrders;
        map.put("status", Orders.CONFIRMED);//待派送
        Integer deliveredOrders = orderMapper.countStatusByMap(map);
        deliveredOrders = deliveredOrders == null ? 0 : deliveredOrders;
        map.put("status", Orders.TO_BE_CONFIRMED);//待接单
        Integer waitingOrders = orderMapper.countStatusByMap(map);
        waitingOrders = waitingOrders == null ? 0 : waitingOrders;
        return OrderOverViewVO.builder().allOrders(allOrders).cancelledOrders(cancelledOrders).completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders).waitingOrders(waitingOrders).build();
    }
}
