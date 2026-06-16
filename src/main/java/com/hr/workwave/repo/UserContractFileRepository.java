package com.hr.workwave.repo;

import com.hr.workwave.model.UserContractFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserContractFileRepository extends JpaRepository<UserContractFile, Long> {

    List<UserContractFile> findByUserIdOrderByCreatedAtDesc(BigInteger userId);

    Optional<UserContractFile> findByIdAndUserId(Long id, BigInteger userId);
}

