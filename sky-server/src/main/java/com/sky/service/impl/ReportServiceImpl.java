package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    @Autowired
    private WorkSpaceService workSpaceService;

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

    /**
     * 导出最近30天的运营数据
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //查询30天的数据
        BusinessDataVO businessData = workSpaceService.getBusinessDate(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            //设置30天总览数据
            XSSFWorkbook excel = new XSSFWorkbook(is);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            //设置每天的明细数据
            for(int i = 0;i < 30;++i){
                row = sheet.getRow(i + 7);
                LocalDate date = begin.plusDays(i);
                businessData = workSpaceService.getBusinessDate(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //写入response
            OutputStream os = response.getOutputStream();
            excel.write(os);
            //关闭资源
            os.flush();
            os.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
