package com.hr.workwave.services;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.UserManagerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UserManagerService {

    private final UserManagerRepository userManagerRepository;

    public List<UserManagers> getManagersForUser(BigInteger userId) {
        return userManagerRepository.findByUserId(userId);
    }

    @Transactional
    public void syncManagersForUser(BigInteger userId, List<BigInteger> managerIds) {
        List<UserManagers> existing = userManagerRepository.findByUserId(userId);

        Set<BigInteger> newIds = managerIds != null ? new HashSet<>(managerIds) : Collections.emptySet();

        Set<BigInteger> existingIds = existing.stream()
                .map(UserManagers::getManagerId)
                .collect(Collectors.toSet());

        for (BigInteger id : newIds) {
            userManagerRepository.insertIgnoreConflict(userId, id);
        }

        for (UserManagers um : existing) {
            if (!newIds.contains(um.getManagerId())) {
                userManagerRepository.delete(um);
            }
        }
    }
}
