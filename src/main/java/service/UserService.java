package service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UserRepository;
import entity.User;

import java.util.Optional;

@Service
public class UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User createUser(String username, String email, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        return repo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email.toLowerCase());
    }

    public boolean checkPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
