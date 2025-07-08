package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status) " +
            "VALUES (#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, #{userName}, #{consignee}, #{cancelReason}, #{rejectionReason}, #{cancelTime}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{deliveryTime}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     * @param number
     * @param userId
     * @return
     */
    @Select("SELECT * FROM orders WHERE number = #{number} AND user_id = #{userId}")
    Orders getByNumberAndUserId(String number, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单信息分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Orders getById(Long id);

    /**
     * 统计对应状态订单的数目
     * @param status
     * @return
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 查找对应状态，时间小于参数的订单
     * @param status
     * @param time
     * @return
     */
    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    /**
     * 根据条件动态查询营业额
     * @param map
     * @return
     */
    Double sumAmountByMap(Map<String, Object> map);
}
