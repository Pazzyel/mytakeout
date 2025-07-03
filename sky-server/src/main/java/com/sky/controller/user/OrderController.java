package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
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
}
