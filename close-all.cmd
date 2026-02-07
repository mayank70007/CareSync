@echo off
setlocal enabledelayedexpansion
title Close Microservices

echo Stopping microservices, gateway, discovery, and gateway terminals...

REM 1) Try to close started windows by their titles (created in run-all.cmd)
for %%T in ("Discovery" "Monolith" "Identity-service" "Doctor-service" "Patient-service" "Appointment-service" "Medical-Record-service" "Billing-service" "Dashboard-service" "Gateway") do (
  taskkill /FI "WINDOWTITLE eq %%~T" /T /F >nul 2>&1
)


REM 2) Kill processes that are listening on known ports
for %%P in (8761 8080 8081 8085 8086 8088 8091 8092 8093 8094) do (
  for /f "tokens=5" %%A in ('netstat -ano ^| findstr /R /C:":%%P[ ]" ^| findstr /I LISTENING') do (
    if not "%%A"=="0" (
      echo Killing PID %%A on port %%P
      taskkill /PID %%A /T /F >nul 2>&1
    )
  )
)

REM 3) As a fallback, kill any java processes running our JARs by command line
powershell -NoProfile -Command " $names = @('discovery-server-0.0.1-SNAPSHOT.jar','identity-service-0.0.1-SNAPSHOT.jar','doctor-service-0.0.1-SNAPSHOT.jar','patient-service-0.0.1-SNAPSHOT.jar','appointment-service-0.0.1-SNAPSHOT.jar','medical-record-service-0.0.1-SNAPSHOT.jar','billing-service-0.0.1-SNAPSHOT.jar','dashboard-service-0.0.1-SNAPSHOT.jar','gateway-0.0.1-SNAPSHOT.jar'); $pattern = ($names | ForEach-Object { [regex]::Escape($_) }) -join '|'; Get-WmiObject Win32_Process | Where-Object { $_.CommandLine -and ($_.CommandLine -match $pattern) } | ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue } catch {} } "

REM Extra safety: kill any remaining java.exe processes that include our microservices folder path
powershell -NoProfile -Command " $root = (Get-Location).Path; Get-WmiObject Win32_Process | Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -and ($_.CommandLine -match [regex]::Escape($root)) } | ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue } catch {} } "

REM Note: WMIC-based process enumeration removed for reliability on newer Windows.

echo Done.
endlocal
