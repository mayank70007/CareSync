package com.example.dashboard.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
  private final RestTemplate restTemplate;
  public DashboardController(RestTemplate restTemplate){ this.restTemplate = restTemplate; }

  @GetMapping("/stats")
  public ResponseEntity<Map<String,Object>> stats(HttpServletRequest request){
    try {
      String auth = request.getHeader("Authorization");
      HttpHeaders headers = new HttpHeaders();
      if(auth != null && !auth.isBlank()) headers.set("Authorization", auth);
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      int doctors = safeCount("http://localhost:8086/doctor", entity);
      int patients = safeCount("http://localhost:8088/patient", entity);
      int appts = safeCount("http://localhost:8091/appointment", entity);
      int bills = safeCount("http://localhost:8093/bill", entity);
      return ResponseEntity.ok(Map.of(
          "doctors", doctors,
          "patients", patients,
          "appointments", appts,
          "bills", bills
      ));
    } catch(Exception e){
      return ResponseEntity.ok(Map.of("error", e.getMessage()));
    }
  }

  private int safeCount(String url, HttpEntity<Void> entity){
    try{
      var resp = restTemplate.exchange(url, HttpMethod.GET, entity, java.util.List.class);
      java.util.List<?> list = resp.getBody();
      return list == null ? 0 : list.size();
    }catch(Exception ex){
      return 0;
    }
  }
}

