package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品: {}", categoryId);
        //先查询redis
        String key = "dish_" + categoryId;
        List<DishVO> redislist = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (redislist != null && redislist.size() > 0) {
            return Result.success(redislist);//缓存存在
        }

        //缓存不存在，查询数据库
        Dish dish = Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        List<DishVO> dishVOList = dishService.listWithFlavor(dish);
        //不存在时重新缓存
        redisTemplate.opsForValue().set(key, dishVOList);
        return Result.success(dishVOList);
    }
}
