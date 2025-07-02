package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    /**
     * 插入地址数据
     * @param addressBook
     */
    @Insert("INSERT INTO address_book (user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "VALUES (#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    /**
     * 动态查询所有地址信息
     * @param build
     * @return
     */
    List<AddressBook> list(AddressBook build);

    /**
     * 动态更新地址信息
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Select("SELECT * FROM address_book WHERE id = #{id}")
    AddressBook getById(Long id);

    /**
     * 根据id删除地址
     * @param id
     */
    @Delete("DELETE FROM address_book WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 根据用户id设置is_default状态
     * @param addressBook
     */
    @Update("UPDATE address_book SET is_default = #{isDefault} WHERE user_id = #{userId}")
    void updateIsDefaultByUserId(AddressBook addressBook);
}
