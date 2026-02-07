package com.example.doctor.config;

import com.example.doctor.model.Doctor;
import com.example.doctor.model.User;
import com.example.doctor.repository.DoctorRepository;
import com.example.doctor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedDoctorService {
    private final DoctorRepository repo;
    private final UserRepository users;

    public SeedDoctorService(DoctorRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional
    public void seedDemoDoctorIfNeeded() {
        if (repo.findByUserUsername("doctor").isPresent()) return;
        Doctor d = new Doctor();
        d.setName("Dr. Demo");
        d.setSpecialization("General");
        d.setPhone("9999999999");
        d.setEmail("doctor@example.com");
        var existing = users.findByUsername("doctor");
        if (existing.isPresent()) {
            d.setUser(existing.get());
        } else {
            User nu = new User();
            nu.setUsername("doctor");
            nu.setRole("ROLE_DOCTOR");
            users.save(nu);
            d.setUser(nu);
        }
        repo.save(d);
        System.out.println("[doctor-service] Bootstrapped demo doctor linked to username 'doctor'.");
    }
}
