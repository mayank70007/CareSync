package com.example.medicalrecord.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MedicalRecord {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long patientId;
  private String diagnosis;
  private String treatment;
  private LocalDate recordDate;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPatientId() { return patientId; }
  public void setPatientId(Long patientId) { this.patientId = patientId; }
  public String getDiagnosis() { return diagnosis; }
  public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
  public String getTreatment() { return treatment; }
  public void setTreatment(String treatment) { this.treatment = treatment; }
  public LocalDate getRecordDate() { return recordDate; }
  public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
}
