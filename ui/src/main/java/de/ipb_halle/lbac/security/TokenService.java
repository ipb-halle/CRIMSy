/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author halocal
 */
@ApplicationScoped
public class TokenService {
    private Map<String, String> tokenStore = new ConcurrentHashMap<>();
    
    public String generateToken(String username) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, username);
        return token;
    }
    
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }
    
    public String getUsernameFromToken(String token) {
        return tokenStore.get(token);
    }
    
    public void revokeToken(String token) {
        tokenStore.remove(token);
    }
}
