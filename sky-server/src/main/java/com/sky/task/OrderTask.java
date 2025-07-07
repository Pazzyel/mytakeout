package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")//在任意分钟的0秒触发
    public void processTimeOutOrder() {
        log.info("处理支付超时订单: {}", new Date());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);//当前时间早15分钟的时间
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("支付超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 定时处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//在任意天的1:00:00时触发
    public void processDeliveryOrder() {
        log.info("处理派送中订单: {}", new Date());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);//当前时间早60分钟的时间，由于每天1点触发，实际上的时间就是今天0点
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);
        if (orders != null && !orders.isEmpty()) {
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
