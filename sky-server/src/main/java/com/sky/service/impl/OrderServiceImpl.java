package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;

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

        //检查是否超出配送范围 //TODO 需要申请百度开发ak，较麻烦，只写上代码，不实际作用
        //checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

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

    /**
     * 检查是否超出配送范围，超出抛出异常
     * @param address
     */
    private void checkOutOfRange(String address){
        //获取商家经纬度
        Map<String,String> map = new HashMap<>();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        String shopAddressInfoJSON = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3",map);
        JSONObject shopAddressInfoJSONObject = JSONObject.parseObject(shopAddressInfoJSON);
        if(!shopAddressInfoJSONObject.getString("status").equals("0")){
            throw new OrderBusinessException("收获地址解析失败");
        }
        JSONObject shopCoordinateJSONObject = shopAddressInfoJSONObject.getJSONObject("result").getJSONObject("location");
        String shopCoordinate = shopCoordinateJSONObject.getString("lat") + "," + shopCoordinateJSONObject.getString("lng");

        //获取用户的经纬度
        map.put("address",address);
        String userAddressInfoJSON = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3",map);
        JSONObject userAddressInfoJSONObject = JSONObject.parseObject(userAddressInfoJSON);
        if(!userAddressInfoJSONObject.getString("status").equals("0")){
            throw new OrderBusinessException("用户地址解析失败");
        }
        JSONObject userCoordinateJSONObject = userAddressInfoJSONObject.getJSONObject("result").getJSONObject("location");
        String userCoordinate = userCoordinateJSONObject.getString("lat") + "," + userCoordinateJSONObject.getString("lng");

        //获取路线信息
        map.clear();
        map.put("origin",shopCoordinate);
        map.put("destination",userCoordinate);
        map.put("ak",ak);
        map.put("steps_info","0");
        String routeJSON = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving",map);
        JSONObject routeJSONObject = JSONObject.parseObject(routeJSON);
        if(!routeJSONObject.getString("status").equals("0")){
            throw new OrderBusinessException("路线解析失败");
        }
        JSONArray routesArray = routeJSONObject.getJSONObject("result").getJSONArray("routes");
        Integer distance = routesArray.getJSONObject(0).getInteger("distance");
        //超出配送范围
        if(distance > 5000){
            throw new OrderBusinessException("超出配送范围");
        }

    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口获得结果
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),
                new BigDecimal(0.01),
                "苍穹外卖订单",
                user.getOpenid());

        //如果重复支付订单，抛出异常
        if(jsonObject.get("code") != null && jsonObject.get("code").equals("ORDERPAID")){
            throw new OrderBusinessException("订单已支付");
        }

        //构建返回对象
        OrderPaymentVO orderPaymentVO = jsonObject.toJavaObject(OrderPaymentVO.class);
        orderPaymentVO.setPackageStr(jsonObject.getString("package"));
        return orderPaymentVO;
    }

    /**
     * 支付成功修改订单状态，真实情况下是接收到微信的支付成功信息后回调
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        Long userId = BaseContext.getCurrentId();
        Orders queryOrder = orderMapper.getByNumberAndUserId(outTradeNo, userId);
        //更新订单状态，支付状态，结账时间
        Orders order = Orders.builder()
                .id(queryOrder.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now()).build();
        orderMapper.update(order);

        // 支付成功用WebSocket通知管理端
        Map<String, Object> map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",order.getId());
        map.put("content","订单号:" + outTradeNo);
        webSocketServer.sendToAllClients(JSON.toJSONString(map));
    }

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.list(ordersPageQueryDTO);

        List<Orders> orders = page.getResult();
        List<OrderVO> vo = new ArrayList<>(orders.size());
        //查询对应订单细节
        for(Orders order : orders){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
            orderVO.setOrderDetailList(orderDetailList);
            vo.add(orderVO);
        }

        return new PageResult(page.getTotal(),vo);
    }

    /**
     * 获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getById(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder().id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason()).build();
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //再来一单是把商品重新加入购物车
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCarts = new ArrayList<>(list.size());
        Long userId = BaseContext.getCurrentId();
        for(OrderDetail orderDetail : list){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setConfirmed(confirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        vo.setToBeConfirmed(toBeConfirmed);
        return vo;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //拒绝订单也要记录取消时间
        Orders orders = Orders.builder().id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now()).build();
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).deliveryStatus(1).build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.COMPLETED).deliveryTime(LocalDateTime.now()).build();//完成时订单送达
        orderMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);//1为来单提醒 2为客户催单
        map.put("orderId",id);
        map.put("content","订单号:" + orders.getNumber());
        webSocketServer.sendToAllClients(JSON.toJSONString(map));
    }
}
