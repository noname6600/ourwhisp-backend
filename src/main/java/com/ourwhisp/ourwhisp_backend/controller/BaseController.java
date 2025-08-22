package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.ApiResponse;
import com.ourwhisp.ourwhisp_backend.dto.IResponseFactory;
import com.ourwhisp.ourwhisp_backend.factory.base.IDataFactory;
import com.ourwhisp.ourwhisp_backend.factory.base.BasePersistDataFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class BaseController<ID, InfoDTO, DetailDTO, Entity> {

    protected final IResponseFactory responseFactory;
    protected final BasePersistDataFactory<ID, InfoDTO, DetailDTO, ?, Entity, ?> persistFactory;
    protected final IDataFactory<ID, InfoDTO, DetailDTO> dataFactory;

    protected BaseController(IResponseFactory responseFactory,
                             BasePersistDataFactory<ID, InfoDTO, DetailDTO, ?, Entity, ?> persistFactory,
                             IDataFactory<ID, InfoDTO, DetailDTO> dataFactory) {
        this.responseFactory = responseFactory;
        this.persistFactory = persistFactory;
        this.dataFactory = dataFactory;
    }

    public ApiResponse<DetailDTO> create(ID id, DetailDTO dto) {
        DetailDTO saved = persistFactory.save(id, dto, (IDataFactory<ID, InfoDTO, DetailDTO>) dataFactory);
        return responseFactory.success(saved, "Created successfully");
    }

    public ApiResponse<DetailDTO> getById(ID id) {
        Optional<DetailDTO> detail = persistFactory.findById(id, (IDataFactory<ID, InfoDTO, DetailDTO>) dataFactory);
        return detail.map(responseFactory::success)
                .orElseGet(() -> responseFactory.error("Entity not found"));
    }

    public ApiResponse<List<InfoDTO>> getAll() {
        List<DetailDTO> all = persistFactory.findAll((IDataFactory<ID, InfoDTO, DetailDTO>) dataFactory);
        List<InfoDTO> infos = all.stream()
                .map(dataFactory::toInfo)
                .collect(Collectors.toList());
        return responseFactory.success(infos);
    }

    public ApiResponse<DetailDTO> update(ID id, DetailDTO dto) {
        DetailDTO updated = persistFactory.save(id, dto, (IDataFactory<ID, InfoDTO, DetailDTO>) dataFactory);
        return responseFactory.success(updated, "Updated successfully");
    }

    public ApiResponse<Void> delete(ID id) {
        persistFactory.delete(id);
        return responseFactory.success(null, "Deleted successfully");
    }
}
