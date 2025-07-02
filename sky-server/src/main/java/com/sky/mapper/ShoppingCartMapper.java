package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 添加购物车条目
     * @param shoppingCart
     */
    @Insert("INSERT INTO shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "VALUES (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车数目
     * @param shoppingCart
     */
    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);
}
