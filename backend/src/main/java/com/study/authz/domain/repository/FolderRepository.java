package com.study.authz.domain.repository;

import com.study.authz.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
