package com.example.patient.config;

import com.example.patient.model.Patient;
import com.example.patient.model.User;
import com.example.patient.repository.PatientRepository;
import com.example.patient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatientDemoBootstrap {

    @Value("${demo.bootstrap.enabled:true}")
    private boolean enabled;

    @Bean
    CommandLineRunner seedPatient(PatientRepository repo, UserRepository users, SeedPatientService seeder){
        return args -> {
            if(!enabled) return;
            seeder.seedDemoPatientIfNeeded();
        };
    }
}
