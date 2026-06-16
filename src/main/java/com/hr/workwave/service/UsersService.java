package com.hr.workwave.service;

import com.hr.workwave.dto.*;
import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.Project;
import com.hr.workwave.model.UserManagers;
import com.hr.workwave.model.User;
import com.hr.workwave.model.UserContractFile;
import com.hr.workwave.repo.ProjectRepository;
import com.hr.workwave.repo.UserContractFileRepository;
import com.hr.workwave.repo.UserManagerRepository;
import com.hr.workwave.repo.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;
    private final UserManagerRepository userManagerRepository;
    private final UserManagerService userManagerService;
    private final ProjectRepository projectRepository;
    private final UserContractFileRepository userContractFileRepository;
    private final UserContractStorageService userContractStorageService;


    /**
     * Retrieves all users from the repository.
     *
     * @return a list of all User entities
     */

    public List<User> getAllUsers() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User loggedUser = usersRepository.findByEmail(email);
        if (loggedUser == null) {
            throw new RuntimeException("User not found");
        }


        if ("ADMIN".equalsIgnoreCase(loggedUser.getRole().getRole())) {
            return usersRepository.findAll();
        }


        if ("MANAGER".equalsIgnoreCase(loggedUser.getRole().getRole())) {
            List<UserManagers> links = userManagerRepository.findByManagerId(loggedUser.getId());
            return links.stream()
                    .map(link -> usersRepository.findById(link.getUserId()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
        }


        return new ArrayList<>();
    }

    public List<UserWithManagersDTO> getAllUsersWithManagers() {
        List<Object[]> results = usersRepository.findAllUsersWithManagers();

        Map<BigInteger, UserWithManagersDTO> userMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            BigInteger userId = ((Number) row[0]).longValue() == 0 ? null : BigInteger.valueOf(((Number) row[0]).longValue());
            String email = (String) row[1];
            String name = (String) row[2];
            String department = (String) row[3];
            String role = (String) row[4];
            LocalDateTime createdAt = toLocalDateTime(row[5]);


            LocalDateTime lastLogin = toLocalDateTime(row[6]);
            Boolean notifyManager = (Boolean) row[7];
            LocalDate startOfWork = toLocalDate(row[8]);
            BigInteger projectId = row[9] != null ? BigInteger.valueOf(((Number) row[9]).longValue()) : null;
            BigInteger availableLeaveDays = row[10] != null ? BigInteger.valueOf(((Number) row[10]).longValue()) : null;
            BigInteger managerId = row[11] != null ? BigInteger.valueOf(((Number) row[11]).longValue()) : null;
            String managerName = (String) row[12];
            String managerEmail = (String) row[13];
            LocalDate contractDueDate = toLocalDate(row[14]);
            if (!userMap.containsKey(userId)) {
                userMap.put(userId, new UserWithManagersDTO(
                        userId != null ? userId.longValue() : null,
                        email,
                        name,
                        department,
                        role,
                        createdAt,
                        lastLogin,
                        Boolean.TRUE.equals(notifyManager),
                        startOfWork,
                        projectId,
                        availableLeaveDays,
                        new ArrayList<>(),
                        contractDueDate
                ));
            }

            if (managerId != null) {
                UserWithManagersDTO userDto = userMap.get(userId);
                userDto.getManagers().add(new ManagerDTO(
                        managerId.longValue(),
                        managerName,
                        managerEmail
                ));
            }
        }

        return new ArrayList<>(userMap.values());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime localDateTime) return localDateTime;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        }
        throw new IllegalArgumentException("Unsupported LocalDateTime value type: " + value.getClass().getName());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime().toLocalDate();
        if (value instanceof LocalDateTime localDateTime) return localDateTime.toLocalDate();
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        throw new IllegalArgumentException("Unsupported LocalDate value type: " + value.getClass().getName());
    }

    /**
     * Updates user details and synchronizes the user's managers.
     * Fetches the user by ID, updates their attributes based on the provided DTO,
     * saves the updated user, and updates the manager relationships accordingly.
     *
     * @param userId the ID of the user to update
     * @param dto    data transfer object containing updated user information and manager IDs
     * @return the updated User entity
     * @throws RuntimeException if the user with the specified ID does not exist
     */

    public User updateUserAndManagers(BigInteger userId, UpdateUsersDTO dto) {
        return updateUserAndManagers(userId, dto, List.of());
    }

    public User updateUserAndManagers(BigInteger userId, UpdateUsersDTO dto, List<MultipartFile> contractFiles) {

        User user = getExistingUser(userId);

        user.setName(dto.getName());
        user.setDepartment(dto.getDepartment());
        user.setRole(dto.getRole());
        user.setStartOfWork(dto.getStartOfWork());
        user.setNotifyManager(dto.getNotifyManager());
        user.setAvailableLeaveDays(dto.getAvailableLeaveDays());
        user.setContractDueDate(dto.getContractDueDate());
        if(dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId()).orElseThrow();
            user.setProject(project);
        }

        usersRepository.save(user);

        List<BigInteger> managerIds = dto.getManagerIds();
        userManagerService.syncManagersForUser(userId, managerIds != null ? managerIds : Collections.emptyList());

        List<UserContractFile> storedContracts = userContractStorageService.storeContracts(user, contractFiles);
        if (!storedContracts.isEmpty()) {
            userContractFileRepository.saveAll(storedContracts);
        }

        return user;
    }

    public List<UserContractFileDTO> getUserContractFiles(BigInteger userId) {
        getExistingUser(userId);

        return userContractFileRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(contract -> new UserContractFileDTO(
                        contract.getId(),
                        contract.getFilename(),
                        contract.getCreatedAt()
                ))
                .toList();
    }

    public UserContractDownload getUserContractFile(BigInteger userId, Long contractId) {
        UserContractFile contractFile = getExistingContractFile(userId, contractId);
        Resource resource = userContractStorageService.loadAsResource(
                userId,
                contractFile.getFilename(),
                contractFile.getEncryptionKey(),
                contractFile.getEncryptionIv()
        );
        return new UserContractDownload(contractFile.getFilename(), resource);
    }

    public void deleteUserContractFile(BigInteger userId, Long contractId) {
        UserContractFile contractFile = getExistingContractFile(userId, contractId);
        userContractFileRepository.delete(contractFile);
        userContractStorageService.deleteStoredContract(userId, contractFile.getFilename());
    }

    public User setProjectID(BigInteger userId, BigInteger projectId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        user.setProject(project);

        return usersRepository.save(user);
    }

    public String getProjectNameByUserId(BigInteger userId) {
        return usersRepository.findProjectNameByUserId(userId);
    }

    public User createOrUpdateUser(UserRequestDTO dto, String authEmail) {
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        User existing = usersRepository.findByEmail(dto.getEmail());


        if (existing != null && dto.getRole() != null && !dto.getRole().equals(existing.getRole())) {
            if (authEmail == null || authEmail.isEmpty()) {
                throw new SecurityException("Only administrators can change user roles");
            }

            User actingUser = usersRepository.findByEmail(authEmail);
            if(actingUser == null){

                throw new SecurityException("Authenticated user not found");
            }

            if (!UserRolesEnum.ADMIN.equals(actingUser.getRole())) {
                throw new SecurityException("Only administrators can change user roles");
            }
        }

        if (existing == null) {

            User newUser = new User();
            newUser.setEmail(dto.getEmail());
            newUser.setName(dto.getName());
            newUser.setDepartment(dto.getDepartment());
            newUser.setRole(dto.getRole() != null ? dto.getRole() : UserRolesEnum.EMPLOYEE);
            newUser.setNotifyManager(dto.getNotifyManager() != null ? dto.getNotifyManager() : Boolean.FALSE);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setLastLogin(LocalDateTime.now());
            newUser.setStartOfWork(LocalDate.now());
            return usersRepository.save(newUser);
        } else {

            existing.setName(dto.getName() != null ? dto.getName() : existing.getName());
            existing.setDepartment(dto.getDepartment() != null ? dto.getDepartment() : existing.getDepartment());
            existing.setNotifyManager(dto.getNotifyManager() != null ? dto.getNotifyManager() : existing.getNotifyManager());
            existing.setLastLogin(LocalDateTime.now());
            if (dto.getRole() != null) existing.setRole(dto.getRole());
            return usersRepository.save(existing);
        }
    }

    public User updateLastLogin(String email) {
        User userOpt = usersRepository.findByEmail(email);

        if (userOpt==null) {
            return null;
        }

        userOpt.setLastLogin(LocalDateTime.now());
        return usersRepository.save(userOpt);
    }
    public Map<String, Object> getUserByEmail(String email) {
        Map<String, Object> result = usersRepository.findUserWithManagerByEmail(email);
        if (result == null || result.isEmpty()) {
            // Auto-create a minimal default user when not present
            // TODO
            // From /persons api get the information if the user is employee or not
            // From /persons api get the information for start-of-work

            NewUserDTO newUser = new NewUserDTO();
            newUser.setEmail(email);
            newUser.setStartOfWork(LocalDate.now());
            createNewDefaultUser(newUser);

            // attempt to read back the user record (with manager info)
            result = usersRepository.findUserWithManagerByEmail(email);
            if (result == null || result.isEmpty()) {
                throw new EntityNotFoundException("User not found after creation");
            }
        }

        return result;
    }

    public CalendarStatusDTO getCalendarStatus(String email) {

        boolean isConnected = checkCalendarConnection(email);

        String connectUrl = isConnected ? null : generateCalendarConnectUrl(email);

        return new CalendarStatusDTO(isConnected, connectUrl);
    }

    private boolean checkCalendarConnection(String email) {
        return false;
    }

    private String generateCalendarConnectUrl(String email) {
        return "https://example.com/connect-calendar?email=" + email;
    }
    public List<PotentialManagerDTO> getPotentialManagers(String excludeEmail) {
        List<User> users = usersRepository.findPotentialManagers(excludeEmail);
        return users.stream()
                .map(u -> new PotentialManagerDTO(
                        u.getId().longValue(),
                        u.getEmail(),
                        u.getName(),
                        u.getDepartment(),
                        u.getRole().getRole()
                ))
                .collect(Collectors.toList());
    }

    public List<UserRequestDTO> getManagers(){
        return usersRepository.getManagers();
    }

    public User getUserById(BigInteger userId) {
        Optional<User> user = usersRepository.findById(userId);
        return user.orElseGet(User::new);

    }

    /**
     * Returns all members of the same project as the given user.
     *
     * @param email the email of the requesting user
     * @return list of TeamMemberDTOs (id, name, email, projectId, projectName)
     */
    public List<TeamMemberDTO> getMyTeam(String email) {
        User currentUser = usersRepository.findByEmail(email);

        if (currentUser.getProject() == null) {
            return Collections.emptyList();
        }

        Long projectId = currentUser.getProject().getId();
        String projectName = currentUser.getProject().getProjectName();

        return usersRepository.findByProjectId(projectId)
                .stream()
                .map(u -> new TeamMemberDTO(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        projectId,
                        projectName
                ))
                .collect(Collectors.toList());
    }

    public User createNewDefaultUser (NewUserDTO newUser){
        User user = User.builder()
                .email(newUser.getEmail())
                .startOfWork(newUser.getStartOfWork())
                .role(UserRolesEnum.EMPLOYEE)
                .build();
        return usersRepository.save(user);
    }

    private User getExistingUser(BigInteger userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    private UserContractFile getExistingContractFile(BigInteger userId, Long contractId) {
        getExistingUser(userId);
        return userContractFileRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contract file not found with ID: " + contractId + " for user ID: " + userId));
    }

    public record UserContractDownload(String filename, Resource resource) {
    }
}

