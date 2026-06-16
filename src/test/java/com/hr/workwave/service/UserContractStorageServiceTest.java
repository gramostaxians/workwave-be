package com.hr.workwave.service;

import com.hr.workwave.enums.UserRolesEnum;
import com.hr.workwave.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserContractStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeContractsShouldPersistFilesUnderUserDirectory() throws Exception {
        UserContractStorageService storageService = new UserContractStorageService(tempDir.toString());
        User user = User.builder()
                .id(BigInteger.valueOf(76))
                .email("employee@example.com")
                .role(UserRolesEnum.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .build();

        MockMultipartFile contract = new MockMultipartFile(
                "contracts",
                "My Contract.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        var storedContracts = storageService.storeContracts(user, List.of(contract));

        assertEquals(1, storedContracts.size());
        String storedFilename = storedContracts.getFirst().getFilename();
        assertTrue(storedFilename.startsWith("my_contract-"));
        assertTrue(storedFilename.endsWith(".pdf"));

        Path storedFile = tempDir.resolve("76").resolve(storedFilename);
        assertTrue(Files.exists(storedFile));
        assertEquals("pdf-content", Files.readString(storedFile));
    }

    @Test
    void deleteStoredContractShouldRemoveFileFromDisk() {
        UserContractStorageService storageService = new UserContractStorageService(tempDir.toString());
        User user = User.builder()
                .id(BigInteger.valueOf(18))
                .email("employee@example.com")
                .role(UserRolesEnum.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .build();

        MockMultipartFile contract = new MockMultipartFile(
                "contracts",
                "contract.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "contract".getBytes()
        );

        String storedFilename = storageService.storeContracts(user, List.of(contract)).getFirst().getFilename();
        storageService.deleteStoredContract(user.getId(), storedFilename);

        assertFalse(Files.exists(tempDir.resolve("18").resolve(storedFilename)));
    }
}

