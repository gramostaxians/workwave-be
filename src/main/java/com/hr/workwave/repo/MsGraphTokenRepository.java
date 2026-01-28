package com.hr.workwave.repo;

import com.hr.workwave.model.MsGraphToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface MsGraphTokenRepository extends JpaRepository<MsGraphToken, String> {


    Optional<MsGraphToken> findByUserId(BigInteger userId);

}
