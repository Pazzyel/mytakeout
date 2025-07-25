package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param dto
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key = "#dto.categoryId")
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO dto) {
        log.info("新增套餐: {}", dto);
        setmealService.save(dto);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param queryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO queryDTO) {
        log.info("套餐分页查询: {}", queryDTO);
        PageResult result = setmealService.pageQuery(queryDTO);
        return Result.success(result);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐: {}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐: {}", id);
        SetmealVO result = setmealService.getByIdWithDishes(id);
        return Result.success(result);
    }

    /**
     * 修改套餐
     * @param dto
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO dto) {
        log.info("修改套餐: {}", dto);
        setmealService.updateWithDishes(dto);
        return Result.success();
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @ApiOperation("套餐起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐起售停售: id: {}, status: {}", id, status);
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
