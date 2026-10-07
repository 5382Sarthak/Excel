package com.sarthak.demo.repository;

import com.sarthak.demo.model.ApiUsage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiUsageRepository extends MongoRepository<ApiUsage, String> {
}