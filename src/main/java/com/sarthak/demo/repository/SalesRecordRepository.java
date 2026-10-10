
package com.sarthak.demo.repository;

import com.sarthak.demo.model.SalesRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SalesRecordRepository
        extends MongoRepository<SalesRecord, String> {
}
