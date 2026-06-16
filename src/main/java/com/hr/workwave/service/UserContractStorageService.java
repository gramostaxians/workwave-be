package com.hr.workwave.service;

import com.hr.workwave.model.User;
import com.hr.workwave.model.UserContractFile;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserContractStorageService {

    private static final DateTimeFormatter FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int MAX_BASE_NAME_LENGTH = 120;

    private final Path storageRoot;

    public UserContractStorageService(
            @Value("${app.storage.user-contracts-dir:uploads/user-contracts}") String storageRootDirectory) {
        this.storageRoot = Path.of(storageRootDirectory).toAbsolutePath().normalize();
    }

    public List<UserContractFile> storeContracts(User user, List<MultipartFile> files) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must exist before storing contract files.");
        }
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        Path userDirectory = resolveUserDirectory(user.getId());
        createDirectories(userDirectory);

        List<Path> storedPaths = new ArrayList<>();
        List<UserContractFile> storedContracts = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String storedFilename = generateStoredFilename(file.getOriginalFilename());
                Path targetPath = userDirectory.resolve(storedFilename).normalize();
                ensureWithinDirectory(targetPath, userDirectory);

                file.transferTo(targetPath);
                storedPaths.add(targetPath);
                storedContracts.add(UserContractFile.builder()
                        .user(user)
                        .filename(storedFilename)
                        .build());
            }
        } catch (IOException | IllegalStateException ex) {
            deleteQuietly(storedPaths);
            throw new IllegalStateException("Failed to store contract files.", ex);
        }

        return storedContracts;
    }

    public Resource loadAsResource(BigInteger userId, String filename) {
        Path userDirectory = resolveUserDirectory(userId);
        Path filePath = userDirectory.resolve(filename).normalize();
        ensureWithinDirectory(filePath, userDirectory);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new EntityNotFoundException("Contract file not found on server.");
        }

        return new FileSystemResource(filePath);
    }

    public void deleteStoredContract(BigInteger userId, String filename) {
        Path userDirectory = resolveUserDirectory(userId);
        Path filePath = userDirectory.resolve(filename).normalize();
        ensureWithinDirectory(filePath, userDirectory);

        try {
            Files.deleteIfExists(filePath);
            deleteUserDirectoryIfEmpty(userDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete contract file from server.", ex);
        }
    }

    private Path resolveUserDirectory(BigInteger userId) {
        Path userDirectory = storageRoot.resolve(userId.toString()).normalize();
        ensureWithinDirectory(userDirectory, storageRoot);
        return userDirectory;
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create contract storage directory.", ex);
        }
    }

    private void deleteQuietly(List<Path> paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }

    private void deleteUserDirectoryIfEmpty(Path userDirectory) throws IOException {
        if (!Files.exists(userDirectory) || !Files.isDirectory(userDirectory)) {
            return;
        }

        try (var entries = Files.list(userDirectory)) {
            if (entries.findAny().isEmpty()) {
                Files.deleteIfExists(userDirectory);
            }
        }
    }

    private void ensureWithinDirectory(Path candidate, Path baseDirectory) {
        if (!candidate.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Invalid contract file path.");
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String safeFilename = sanitizeOriginalFilename(originalFilename);
        int lastDotIndex = safeFilename.lastIndexOf('.');

        String extension = lastDotIndex > 0 ? safeFilename.substring(lastDotIndex) : "";
        String baseName = lastDotIndex > 0 ? safeFilename.substring(0, lastDotIndex) : safeFilename;

        if (baseName.length() > MAX_BASE_NAME_LENGTH) {
            baseName = baseName.substring(0, MAX_BASE_NAME_LENGTH);
        }

        return "%s-%s%s".formatted(
                baseName,
                LocalDateTime.now().format(FILENAME_TIMESTAMP) + "-" + UUID.randomUUID(),
                extension
        );
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        String candidate = originalFilename == null ? "contract" : originalFilename.replace('\\', '/');
        int lastSlashIndex = candidate.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            candidate = candidate.substring(lastSlashIndex + 1);
        }

        String sanitized = candidate
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._-]+|[._-]+$", "");

        if (sanitized.isBlank()) {
            return "contract";
        }

        return sanitized.toLowerCase(Locale.ROOT);
    }
}

