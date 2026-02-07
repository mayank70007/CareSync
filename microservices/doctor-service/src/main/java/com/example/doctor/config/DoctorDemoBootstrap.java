package com.example.doctor.config;

import com.example.doctor.model.Doctor;
import com.example.doctor.model.User;
import com.example.doctor.repository.DoctorRepository;
import com.example.doctor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DoctorDemoBootstrap {

    @Value("${demo.bootstrap.enabled:true}")
    private boolean enabled;

    @Bean
    CommandLineRunner seedDoctor(DoctorRepository repo, UserRepository users, SeedDoctorService seeder){
        return args -> {
            if(!enabled) return;
            seeder.seedDemoDoctorIfNeeded();
        };
    }
}
