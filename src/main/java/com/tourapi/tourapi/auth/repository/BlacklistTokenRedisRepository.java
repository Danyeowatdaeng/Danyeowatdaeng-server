package com.tourapi.tourapi.auth.repository;


import com.tourapi.tourapi.auth.entity.BlacklistTokenRedisEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistTokenRedisRepository extends CrudRepository<BlacklistTokenRedisEntity, String> {
} 