package com.zzy.backend.converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础转换器接口
 */
public interface BaseConverter<Entity, DTO, VO> {
    
    /**
     * Entity 转 DTO
     */
    DTO entityToDto(Entity entity);
    
    /**
     * DTO 转 Entity
     */
    Entity dtoToEntity(DTO dto);
    
    /**
     * Entity 转 VO
     */
    VO entityToVo(Entity entity);
    
    /**
     * DTO 转 VO
     */
    VO dtoToVo(DTO dto);
    
    /**
     * Entity 列表转 DTO 列表
     */
    default List<DTO> entityListToDtoList(List<Entity> entities) {
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * DTO 列表转 Entity 列表
     */
    default List<Entity> dtoListToEntityList(List<DTO> dtos) {
        return dtos.stream()
                .map(this::dtoToEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Entity 列表转 VO 列表
     */
    default List<VO> entityListToVoList(List<Entity> entities) {
        return entities.stream()
                .map(this::entityToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * DTO 列表转 VO 列表
     */
    default List<VO> dtoListToVoList(List<DTO> dtos) {
        return dtos.stream()
                .map(this::dtoToVo)
                .collect(Collectors.toList());
    }
}

