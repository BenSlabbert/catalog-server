package com.fluent.catalog.mapper;

import com.fluent.catalog.dao.entiy.Item;
import com.fluent.catalog.web.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemMapper {

  ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

  ItemDto toDto(Item item);
}
