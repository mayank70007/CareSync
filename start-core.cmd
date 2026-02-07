@echo off
echo Starting Core Healthcare Services...
echo.

echo Starting Discovery Server...
start "Discovery" cmd /c "java -jar microservices\discovery-server\target\discovery-server-0.0.1-SNAPSHOT.jar"
timeout /t 10 /nobreak >nul

echo Starting Identity Service...
start "Identity" cmd /c "java -jar microservices\identity-service\target\identity-service-0.0.1-SNAPSHOT.jar"
timeout /t 5 /nobreak >nul

echo Starting Gateway...
start "Gateway" cmd /c "java -jar microservices\gateway\target\gateway-0.0.1-SNAPSHOT.jar"
timeout /t 5 /nobreak >nul

echo Starting Appointment Service...
start "Appointment" cmd /c "java -jar microservices\appointment-service\target\appointment-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

echo Starting Medical Record Service...
start "MedicalRecord" cmd /c "java -jar microservices\medical-record-service\target\medical-record-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

echo.
echo All core services starting...
echo Gateway: http://localhost:8080
echo Login: admin/admin123
echo.
pause