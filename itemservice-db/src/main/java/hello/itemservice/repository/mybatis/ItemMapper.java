package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {

    void save(Item item);

    // parameter가 2개인 경우 @Param으로 구분해야한다.
    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto itemUpdateDto);

    // BeanPropertyRowMapper 처럼 Select 결과를 객체로 바로 변환해준다.
    // @Select("select id, item_name, price, quantity from item where id=#{id}")
    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond itemSearchCond);

}
