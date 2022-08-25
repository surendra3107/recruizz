package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.enums.CustomFieldEntityType;

public interface CustomFieldsRepository extends JpaRepository<CustomFields, Long> {

    List<CustomFields> findByEntityType(CustomFieldEntityType entityType);

}
