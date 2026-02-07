package com.example.patient.config;

import com.example.patient.model.Patient;
import com.example.patient.model.User;
import com.example.patient.repository.PatientRepository;
import com.example.patient.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedPatientService {
    private final PatientRepository repo;
    private final UserRepository users;

    public SeedPatientService(PatientRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional
    public void seedDemoPatientIfNeeded() {
        if (repo.findByUserUsername("patient").isPresent()) return;
        Patient p = new Patient();
        p.setName("John Demo");
        p.setAge(30);
        p.setGender("Male");
        p.setAddress("Demo Street 1");
        p.setPhone("8888888888");
        var existing = users.findByUsername("patient");
        if (existing.isPresent()) {
            // Managed entity within the same persistence context
            p.setUser(existing.get());
        } else {
            User nu = new User();
            nu.setUsername("patient");
            nu.setRole("ROLE_PATIENT");
            users.save(nu); // ensure managed/new user is persisted
            p.setUser(nu);
        }
        repo.save(p);
        System.out.println("[patient-service] Bootstrapped demo patient linked to username 'patient'.");
    }
}
