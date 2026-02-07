package com.example.billing.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Bill {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long patientId;
  private Double amount;
  private LocalDate billDate;
  private String status;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPatientId() { return patientId; }
  public void setPatientId(Long patientId) { this.patientId = patientId; }
  public Double getAmount() { return amount; }
  public void setAmount(Double amount) { this.amount = amount; }
  public LocalDate getBillDate() { return billDate; }
  public void setBillDate(LocalDate billDate) { this.billDate = billDate; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
