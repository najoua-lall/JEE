package com.stage.backend.service;

import com.stage.backend.entity.ERole;
import com.stage.backend.entity.Role;
import com.stage.backend.entity.User;
import com.stage.backend.repository.RoleRepository;
import com.stage.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder encoder;

    @Transactional
    public User registerUser(User user, Set<String> strRoles) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ce nom d'utilisateur est déjà pris.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cet email est déjà utilisé.");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(findRole(ERole.ROLE_ETUDIANT));
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin"     -> roles.add(findRole(ERole.ROLE_ADMIN));
                    case "recruteur" -> roles.add(findRole(ERole.ROLE_RECRUTEUR));
                    default          -> roles.add(findRole(ERole.ROLE_ETUDIANT));
                }
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private Role findRole(ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException(
                        "Rôle introuvable : " + eRole));
    }
}