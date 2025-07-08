package com.sky.service;

import com.sky.vo.BusinessDataVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {
    /**
     * 查询对应时间段运营数据
     * @param beginTime
     * @param endTime
     * @return
     */
    BusinessDataVO getBusinessDate(LocalDateTime beginTime, LocalDateTime endTime);
}
