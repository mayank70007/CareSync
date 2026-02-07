package com.example.identity.config;

import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {

    @Value("${admin.bootstrap.enabled:true}")
    private boolean enabled;
    @Value("${admin.bootstrap.username:admin}")
    private String username;
    @Value("${admin.bootstrap.password:admin123}")
    private String password;
    @Value("${admin.bootstrap.role:ROLE_ADMIN}")
    private String role;
    @Value("${admin.bootstrap.force:false}")
    private boolean force;

    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder){
        return args -> {
            if(!enabled) return;
            repo.findByUsername(username).ifPresentOrElse(u -> {
                if(force){
                    u.setPassword(encoder.encode(password));
                    u.setRole(role);
                    repo.save(u);
                    System.out.println("[identity-service] Admin user '"+username+"' reset (password/role).");
                }
            }, () -> {
                User u = new User();
                u.setUsername(username);
                u.setPassword(encoder.encode(password));
                u.setRole(role);
                repo.save(u);
                System.out.println("[identity-service] Bootstrapped admin user '"+username+"'.");
            });
        };
    }
}
