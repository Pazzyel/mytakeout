package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
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
}
