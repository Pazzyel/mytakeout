package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        //构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //构建对应营业额列表
        List<Double> turnoverList = new ArrayList<>(dateList.size());
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String,Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Double turnover = orderMapper.sumAmountByMap(map);
            if(turnover == null)
                turnover = 0.0;
            turnoverList.add(turnover);
        }

        //构建返回结果
        return TurnoverReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList,",")).build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //构建新增用户和总用户列表
        int N = dateList.size();
        List<Integer> newUserList = new ArrayList<>(N);
        List<Integer> totalUserList = new ArrayList<>(N);
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer newUser = getUserCount(beginTime,endTime);
            Integer totalUser = getUserCount(null,endTime);
            newUserList.add(newUser == null ? 0 : newUser);
            totalUserList.add(totalUser == null ? 0 : totalUser);
        }

        //构建返回结果
        return UserReportVO.builder().dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,",")).build();
    }

    /**
     * 统计两个参数区间内创建的用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map<String,Object> map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return userMapper.countUserByMap(map);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderReport(LocalDate begin, LocalDate end) {
        //构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //查询对应订单数目
        int N = dateList.size();
        List<Integer> orderCountList = new ArrayList<>(N);
        List<Integer> validOrderCountList = new ArrayList<>(N);
        //订单总数和有效订单总数
        Integer totalOrderCount = 0;
        Integer validTotalOrderCount = 0;
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询
            Integer orderCount = getOrderCount(beginTime,endTime,null);
            orderCount = orderCount == null ? 0 : orderCount;
            Integer validOrderCount = getOrderCount(beginTime,endTime,Orders.COMPLETED);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            //累加
            totalOrderCount += orderCount;
            validTotalOrderCount += validOrderCount;
            //添加
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算订单总数完成率
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0 : validTotalOrderCount.doubleValue() / totalOrderCount;
        return OrderReportVO.builder().dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount).build();
    }

    /**
     * 统计事件参数区间内对应状态的订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String,Object> map = new HashMap<>();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);
        return orderMapper.countStatusByMap(map);
    }

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime,endTime);

        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");
        return SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();
    }
}
