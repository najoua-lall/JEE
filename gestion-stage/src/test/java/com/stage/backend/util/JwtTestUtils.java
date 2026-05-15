package com.stage.backend.util;

import com.stage.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Génère de vrais tokens JWT signés pour les tests d'intégration.
 * Utilise le même JwtUtils que l'application — garantit que les tokens
 * sont valides vis-à-vis du filtre AuthTokenFilter.
 */
@Component
public class JwtTestUtils {

    @Autowired
    private JwtUtils jwtUtils;

    public String generateToken(String username, String role) {
        var authorities = List.of(new SimpleGrantedAuthority(role));
        var userDetails = new User(username, "password", authorities);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);
        return jwtUtils.generateJwtToken(auth);
    }

    public String tokenEtudiant(String username) {
        return generateToken(username, "ROLE_ETUDIANT");
    }

    public String tokenRecruteur(String username) {
        return generateToken(username, "ROLE_RECRUTEUR");
    }

    public String tokenAdmin(String username) {
        return generateToken(username, "ROLE_ADMIN");
    }
}