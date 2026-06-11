package com.hr.workwave.service;

import com.hr.workwave.model.UserManagers;
import com.hr.workwave.repo.UserManagerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserManagerService {

    private final UserManagerRepository userManagerRepository;

    /**
     * Retrieves the list of manager relationships for a given user.
     *
     * @param userId the ID of the user whose managers are to be fetched
     * @return a list of UserManagers representing the user's managers
     */

    public List<UserManagers> getManagersForUser(BigInteger userId) {
        return userManagerRepository.findByUserId(userId);
    }

    /**
     * Synchronizes the list of managers assigned to a user.
     * Adds new manager associations that do not already exist,
     * and removes any existing associations that are not in the provided list.
     *
     * @param userId the ID of the user whose managers are being synchronized
     * @param managerIds the list of manager IDs to associate with the user
     */

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
