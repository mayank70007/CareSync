package com.example.identity.config;

import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultUsersBootstrap {

    @Value("${users.bootstrap.enabled:true}")
    private boolean enabled;
    @Value("${users.bootstrap.force:false}")
    private boolean force;

    @Value("${users.bootstrap.doctor.username:doctor}")
    private String doctorUsername;
    @Value("${users.bootstrap.doctor.password:doctor123}")
    private String doctorPassword;
    @Value("${users.bootstrap.doctor.role:ROLE_DOCTOR}")
    private String doctorRole;

    @Value("${users.bootstrap.patient.username:patient}")
    private String patientUsername;
    @Value("${users.bootstrap.patient.password:patient123}")
    private String patientPassword;
    @Value("${users.bootstrap.patient.role:ROLE_PATIENT}")
    private String patientRole;

    @Bean
    CommandLineRunner seedDefaults(UserRepository repo, PasswordEncoder encoder){
        return args -> {
            if(!enabled) return;
            repo.findByUsername(doctorUsername).ifPresentOrElse(u -> {
                if(force){
                    u.setPassword(encoder.encode(doctorPassword));
                    u.setRole(doctorRole);
                    repo.save(u);
                    System.out.println("[identity-service] Reset doctor user '"+doctorUsername+"'.");
                }
            }, () -> {
                User u = new User();
                u.setUsername(doctorUsername);
                u.setPassword(encoder.encode(doctorPassword));
                u.setRole(doctorRole);
                repo.save(u);
                System.out.println("[identity-service] Bootstrapped doctor user '"+doctorUsername+"'.");
            });

            repo.findByUsername(patientUsername).ifPresentOrElse(u -> {
                if(force){
                    u.setPassword(encoder.encode(patientPassword));
                    u.setRole(patientRole);
                    repo.save(u);
                    System.out.println("[identity-service] Reset patient user '"+patientUsername+"'.");
                }
            }, () -> {
                User u = new User();
                u.setUsername(patientUsername);
                u.setPassword(encoder.encode(patientPassword));
                u.setRole(patientRole);
                repo.save(u);
                System.out.println("[identity-service] Bootstrapped patient user '"+patientUsername+"'.");
            });
        };
    }
}
