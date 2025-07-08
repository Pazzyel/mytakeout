package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

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
}
