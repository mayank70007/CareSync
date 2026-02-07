# Test Identity Service Directly
Write-Host "=== Testing Identity Service (Port 8085) ==="
try {
    $directResponse = Invoke-WebRequest -Uri "http://localhost:8085/login" -Method Post -Body '{"username":"admin","password":"admin123"}' -ContentType "application/json"
    Write-Host "Direct Identity Service Response:"
    $directResponse.Content
} catch {
    Write-Host "Direct Identity Service Error: $($_.Exception.Message)"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
}

Write-Host "`n=== Testing Gateway Login (Port 8080) ==="
try {
    $body = '{"username":"admin","password":"admin123"}'
    $response = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body $body -ContentType "application/json"
    Write-Host "Gateway Login Response:"
    $response.Content

    # Parse JWT token for testing authenticated endpoints
    $loginResult = $response.Content | ConvertFrom-Json
    if ($loginResult.token) {
        $token = $loginResult.token
        Write-Host "JWT Token received: $($token.Substring(0,20))..."
        
        # Test authenticated endpoint
        $headers = @{ 'Authorization' = "Bearer $token" }
        Write-Host "`n=== Testing /appointment endpoint with JWT ==="
        $appointmentResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointment" -Method Get -Headers $headers
        Write-Host "Appointment API Response:"
        $appointmentResponse.Content
    }
} catch {
    Write-Host "Gateway Login Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    }
}