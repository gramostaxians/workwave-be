package com.hr.workwave.services;

import com.hr.workwave.dto.MsGraphTokenDTO;
import com.hr.workwave.model.MsGraphToken;
import com.hr.workwave.repo.MsGraphTokenRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class MsGraphTokenService {

    private final MsGraphTokenRepository repository;

    public MsGraphTokenService(MsGraphTokenRepository repository) {
        this.repository = repository;
    }

    public MsGraphToken saveOrUpdate(String userEmail, MsGraphTokenDTO request) {

        MsGraphToken token = repository.findById(userEmail)
                .orElseGet(() -> {
                    MsGraphToken t = new MsGraphToken();
                    t.setUserEmail(userEmail);
                    return t;
                });
        token.setUserId(request.getUserId());
        token.setAccessToken(request.getAccessToken());
        token.setRefreshToken(request.getRefreshToken());

        return repository.save(token);
    }
    public MsGraphToken getTokenByUserId(BigInteger userId) {
        return repository.findByUserId(userId)
                .orElse(null);
    }
}