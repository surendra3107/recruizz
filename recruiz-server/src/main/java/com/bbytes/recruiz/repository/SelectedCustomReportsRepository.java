package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.SelectedCustomField;

public interface SelectedCustomReportsRepository extends JpaRepository<SelectedCustomField, Long> {

}
