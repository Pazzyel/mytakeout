package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")//因为用户端也有一个ShopController，不写名字会有bean冲突
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    private static final String KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result getStatus() {
        Integer status = Integer.parseInt((String) redisTemplate.opsForValue().get(KEY));//返回的Object实际上是字符串
        log.info("获取到的营业状态为: {}", status == 1 ? "营业中" : "已打烊");
        return Result.success(status);
    }
}