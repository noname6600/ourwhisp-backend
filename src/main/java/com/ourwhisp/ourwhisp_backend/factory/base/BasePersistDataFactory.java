package com.ourwhisp.ourwhisp_backend.factory.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 *
 * @param <ID>
 * @param <InfoDTO>
 * @param <DetailDTO>
 * @param <EntityID>
 * @param <Entity>
 * @param <Repo>
 */
public abstract class BasePersistDataFactory<
        ID,
        InfoDTO,
        DetailDTO,
        EntityID,
        Entity,
        Repo extends MongoRepository<Entity, EntityID>
        > {

    protected final Repo repository;

    @Autowired
    protected RedisTemplate<String, DetailDTO> redisTemplate;

    protected final String redisPrefix;
    protected final Duration defaultTtl = Duration.ofMinutes(30);

    public BasePersistDataFactory(Repo repository) {
        this.repository = repository;
        this.redisPrefix = this.getClass().getSimpleName();
    }

    protected String buildRedisKey(ID id) {
        return redisPrefix + ":" + id;
    }

    protected void cachePut(ID id, DetailDTO value) {
        redisTemplate.opsForValue().set(buildRedisKey(id), value, defaultTtl);
    }

    protected Optional<DetailDTO> cacheGet(ID id) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildRedisKey(id)));
    }

    protected void cacheEvict(ID id) {
        redisTemplate.delete(buildRedisKey(id));
    }

    public DetailDTO save(ID id,
                          DetailDTO detail,
                          IPersistDataFactory<InfoDTO, DetailDTO, Entity> mapper) {
        Entity entity = mapper.toEntity(detail);
        Entity saved = repository.save(entity);
        DetailDTO result = mapper.toDetail(saved);
        cachePut(id, result);
        return result;
    }

    public Optional<DetailDTO> findById(ID id,
                                        EntityID entityId,
                                        IPersistDataFactory<InfoDTO, DetailDTO, Entity> mapper) {
        return cacheGet(id).or(() ->
                repository.findById(entityId).map(e -> {
                    DetailDTO detail = mapper.toDetail(e);
                    cachePut(id, detail);
                    return detail;
                })
        );
    }

    public List<DetailDTO> findAll(IPersistDataFactory<InfoDTO, DetailDTO, Entity> mapper) {
        return repository.findAll().stream()
                .map(mapper::toDetail)
                .collect(Collectors.toList());
    }

    public void delete(ID id, EntityID entityId) {
        repository.deleteById(entityId);
        cacheEvict(id);
    }

    public boolean existsById(EntityID entityId) {
        return repository.existsById(entityId);
    }
}
