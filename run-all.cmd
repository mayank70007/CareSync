@echo off
setlocal

REM Globally disable Spring Cloud's compatibility verifier for this session
set SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false

REM Change to the directory where this script resides (repo root)
pushd %~dp0

echo.
echo Optional: build all modules first (uncomment next line if needed)
REM call mvnw -q -DskipTests package

echo Building microservices (skip tests)...
call mvnw -q -DskipTests -f microservices\discovery-server\pom.xml package
call mvnw -q -DskipTests -f microservices\identity-service\pom.xml package
call mvnw -q -DskipTests -f microservices\doctor-service\pom.xml package
call mvnw -q -DskipTests -f microservices\patient-service\pom.xml package
call mvnw -q -DskipTests -f microservices\appointment-service\pom.xml package
call mvnw -q -DskipTests -f microservices\medical-record-service\pom.xml package
call mvnw -q -DskipTests -f microservices\billing-service\pom.xml package
call mvnw -q -DskipTests -f microservices\dashboard-service\pom.xml package
call mvnw -q -DskipTests -f microservices\gateway\pom.xml package

echo Starting Discovery Server (8761)...
start "Discovery" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\discovery-server\target\discovery-server-0.0.1-SNAPSHOT.jar

REM Give the registry a moment to come up (increase if needed)
timeout /t 10 /nobreak >nul
echo Waiting 10 seconds for Eureka to be ready...

REM Monolith removed; UI is now served from Gateway

REM Small delay to stagger startups
timeout /t 2 /nobreak >nul


echo Starting Identity-service (8085)...
start "Identity-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\identity-service\target\identity-service-0.0.1-SNAPSHOT.jar

echo Starting Doctor-service (8086)...
start "Doctor-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\doctor-service\target\doctor-service-0.0.1-SNAPSHOT.jar

echo Starting Patient-service (8088)...
start "Patient-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\patient-service\target\patient-service-0.0.1-SNAPSHOT.jar

echo Starting Appointment-service (8091)...
start "Appointment-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\appointment-service\target\appointment-service-0.0.1-SNAPSHOT.jar

echo Starting Medical-Record-service (8092)...
start "Medical-Record-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\medical-record-service\target\medical-record-service-0.0.1-SNAPSHOT.jar

echo Starting Billing-service (8093)...
start "Billing-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\billing-service\target\billing-service-0.0.1-SNAPSHOT.jar

echo Starting Dashboard-service (8094)...
start "Dashboard-service" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\dashboard-service\target\dashboard-service-0.0.1-SNAPSHOT.jar

echo Starting Gateway (8080)...
start "Gateway" cmd /k java -Dspring.cloud.compatibility-verifier.enabled=false -jar microservices\gateway\target\gateway-0.0.1-SNAPSHOT.jar

echo.
echo All services launched in separate terminals. Browse http://localhost:8080

echo Done.
endlocal
exit /b 0
