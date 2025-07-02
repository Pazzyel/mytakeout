package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //地址为空时抛出异常
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车为空时抛出异常
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();//查询这个用户的购物车条目
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构建保存对象
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//用当前系统时间戳作订单号
        orders.setStatus(Orders.PENDING_PAYMENT);//等待支付
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        //还没确认，不设置这个字段
        orders.setPayStatus(Orders.UN_PAID);//未支付
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        //userName字段作用未知，不设置
        orders.setConsignee(addressBook.getConsignee());
        //没有取消或拒绝，没有预测送达时间，这些字段也不设置
        orderMapper.insert(orders);

        //保存订单详情信息
        List<OrderDetail> orderDetails = new ArrayList<>(list.size());
        for(ShoppingCart sc : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc, orderDetail);
            orderDetail.setOrderId(orders.getId());//只有orderId字段是ShoppingCart没有的
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        shoppingCartMapper.deleteByUserId(userId);//下单完成后要清空购物车数据

        //构建返回对象
        return OrderSubmitVO.builder().id(orders.getId())
                .orderNumber(orders.getNumber()).orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime()).build();
    }
}
