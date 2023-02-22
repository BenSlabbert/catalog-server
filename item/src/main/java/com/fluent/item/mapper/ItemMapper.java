package com.fluent.item.mapper;

import com.fluent.item.dao.entity.Item;
import com.fluent.item.web.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemMapper {

  ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

  ItemDto toDto(Item item);
}
