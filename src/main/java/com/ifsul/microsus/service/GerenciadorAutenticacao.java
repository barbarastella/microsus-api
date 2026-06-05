package com.ifsul.microsus.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorAutenticacao {
    private static final String USUARIO = "admin";
    private static final String SENHA = "123456";
    
    private final Set<String> tokensValidos; // set desconsidera valores duplicados

    public GerenciadorAutenticacao() {
        this.tokensValidos = ConcurrentHashMap.newKeySet(); // controle de concorrência
    }
    
    public String login(String usuario, String senha) {
        if (USUARIO.equals(usuario) && SENHA.equals(senha)) {
            String novoToken = UUID.randomUUID().toString();
            tokensValidos.add(novoToken);
            
            return novoToken;
        }
        
        return null;
    }
    
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        
        return tokensValidos.contains(token);
    }
}
