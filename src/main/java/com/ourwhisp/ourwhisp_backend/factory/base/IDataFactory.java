package com.ourwhisp.ourwhisp_backend.factory.base;

import java.util.List;

public interface IDataFactory<UUID, InfoDTO, DetailDTO> {
    InfoDTO toInfoDto(DetailDTO detail);
}
