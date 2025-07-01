package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    /**
     * 根据categoryId查询套餐数量
     * @param categoryId
     * @return
     */
    @Select("SELECT COUNT(*) FROM setmeal WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")//必须有keyProperty指定回填字段
    @Insert("INSERT INTO setmeal (category_id, name, price, description, image, create_time, update_time, create_user, update_user) " +
            "VALUES (#{categoryId}, #{name}, #{price}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Setmeal setmeal);

    /**
     * 套餐分页查询
     * @param queryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO queryDTO);

    /**
     * 统计在售套餐数目
     * @param ids
     * @return
     */
    Integer countOnSale(List<Long> ids);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("SELECT * FROM setmeal WHERE id = #{id}")
    Setmeal getById(Long id);

    /**
     * 更新套餐信息
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询包含的菜品
     * @param setmealId
     * @return
     */
    @Select("SELECT sd.copies, d.description, d.image, sd.name FROM setmeal_dish sd LEFT JOIN dish d ON sd.dish_id = d.id " +
            "WHERE setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
