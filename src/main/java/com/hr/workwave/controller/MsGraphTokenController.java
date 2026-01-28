package com.hr.workwave.controller;

import com.hr.workwave.dto.MsGraphTokenDTO;
import com.hr.workwave.model.MsGraphToken;
import com.hr.workwave.services.MsGraphTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/ms-graph")
public class MsGraphTokenController {

    private final MsGraphTokenService service;

    public MsGraphTokenController(MsGraphTokenService service) {
        this.service = service;
    }


    @PutMapping("/token")
    public ResponseEntity<MsGraphToken> saveOrUpdateToken(
            @RequestParam String userEmail,
            @RequestBody MsGraphTokenDTO request
    ) {
        MsGraphToken token = service.saveOrUpdate(userEmail, request);
        return ResponseEntity.ok(token);
    }
    @GetMapping("/token")
    public ResponseEntity<MsGraphToken> getTokenByUserId(@RequestParam BigInteger userId) {
        MsGraphToken token = service.getTokenByUserId(userId);
        if (token != null) {
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
