package com.example.patient.repository;

import com.example.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByNameContainingIgnoreCase(String name);
    List<Patient> findByAge(int age);
    List<Patient> findByGenderIgnoreCase(String gender);
    Optional<Patient> findByUserUsername(String username);
}
