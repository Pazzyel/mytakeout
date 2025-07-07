package com.sky.controller.user;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付: {}", ordersPaymentDTO);

        //真实的支付逻辑
        //OrderPaymentVO vo = orderService.payment(ordersPaymentDTO);

        //由于个人无法申请商户号，用测试的支付逻辑替代真实的
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("testNonce").packageStr("testPackage").paySign("testPaySign").signType("testSignType")
                .timeStamp(String.valueOf(System.currentTimeMillis())).build();
        //自行回调支付成功逻辑
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());

        log.info("生成预支付交易单: {}", vo);
        return Result.success(vo);
    }

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单分页查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("历史订单分页查询: {}", ordersPageQueryDTO);
        PageResult result = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("获取订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("获取订单详情: {}", id);
        OrderVO vo = orderService.getById(id);
        return Result.success(vo);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单: {}", id);
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO();
        ordersCancelDTO.setId(id);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PutMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单: {}", id);
        orderService.repetition(id);
        return Result.success();
    }

}
