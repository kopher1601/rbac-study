package com.study.authz.domain.repository;

import com.study.authz.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
