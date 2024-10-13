package com.api.pmtool.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.pmtool.entity.Uploads;

public interface UploadRepository extends JpaRepository<Uploads, UUID> {}