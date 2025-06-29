package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 统计categoryId下的菜品数量
     * @param categoryId
     * @return
     */
    @Select("SELECT COUNT(*) FROM dish WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO dish ( name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)" +
            "VALUES (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     * @param id
     */
    @Delete("DELETE FROM dish WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getById(Long id);

    /**
     * 计算在售菜品的数量
     * @param ids
     * @return
     */
    Integer countOnSale(List<Long> ids);
}
