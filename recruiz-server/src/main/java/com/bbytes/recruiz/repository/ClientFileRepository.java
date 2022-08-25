package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.ClientFile;

public interface ClientFileRepository extends JpaRepository<ClientFile, Long> {
    List<ClientFile> findByClientId(String clientId);

    @Query(value = "select * from client_file where storageMode = 'aws'", nativeQuery = true)
	List<ClientFile> getClientFileByStorageModeAWS();
}
