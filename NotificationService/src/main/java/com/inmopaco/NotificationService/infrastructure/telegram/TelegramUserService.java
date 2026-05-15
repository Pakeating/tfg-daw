package com.inmopaco.NotificationService.infrastructure.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class TelegramUserService {

    @Value("${telegram.users.file:users.json}")
    private String usersFilePath;

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Long> users;

    public TelegramUserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.users = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        loadUsers();
    }

    public void addUser(Long chatId) {
        users.put(chatId, Instant.now().toEpochMilli());
        saveUsers();
    }

    public boolean hasUser(Long chatId) {
        return users.containsKey(chatId);
    }

    public List<Long> getAllChatIds() {
        return new ArrayList<>(users.keySet());
    }

    private void loadUsers() {
        Path path = Paths.get(usersFilePath);
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                List<Long> loaded = objectMapper.readValue(json, new TypeReference<List<Long>>() {});
                loaded.forEach(id -> users.put(id, Instant.now().toEpochMilli()));
                log.info("Cargados {} usuarios de {}", loaded.size(), usersFilePath);
            } catch (IOException e) {
                log.error("Error cargando usuarios: {}", e.getMessage());
            }
        }
    }

    private void saveUsers() {
        try {
            String json = objectMapper.writeValueAsString(users.keySet().stream().toList());
            Files.writeString(Paths.get(usersFilePath), json);
        } catch (IOException e) {
            log.error("Error guardando usuarios: {}", e.getMessage());
        }
    }
}