package com.ourwhisp.ourwhisp_backend.factory.base;

/**
 *
 * @param <InfoDTO>
 * @param <DetailDTO>
 * @param <Entity>
 */
public interface IPersistDataFactory<InfoDTO, DetailDTO, Entity> {
    DetailDTO toDetail(Entity entity);
    InfoDTO toInfo(Entity entity);
    Entity toEntity(DetailDTO dto);
}
