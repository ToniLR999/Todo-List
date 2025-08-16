# Script para gestionar horarios en Railway (PowerShell)
# Ejecutar como tarea programada: 0 9 * * 1-5 (Lunes a Viernes a las 9:00)
# Ejecutar como tarea programada: 0 19 * * 1-5 (Lunes a Viernes a las 19:00)

$RAILWAY_APP_ID = "2984dca1-0f61-43b5-a3d3-226ee13fc9d0"
$RAILWAY_MYSQL_ID = "TU_MYSQL_SERVICE_ID_AQUI"  # Necesitas encontrar este ID
$RAILWAY_TOKEN = "8b4989af-d29e-4e54-8385-35f3257b33e4"
$RAILWAY_PROJECT_ID = "8e5843ac-2088-4c98-b09a-7359a4a3c30e"

# Función para verificar si es horario de funcionamiento
function Test-BusinessHours {
    $currentHour = (Get-Date).Hour
    $currentMinute = (Get-Date).Minute
    $currentTime = $currentHour * 60 + $currentMinute
    
    # Horario: 10:00 AM - 7:00 PM (Madrid)
    $startTime = 10 * 60   # 10:00 AM
    $endTime = 19 * 60     # 7:00 PM
    
    # Solo lunes a viernes
    $currentDay = (Get-Date).DayOfWeek.value__  # 1=Domingo, 2=Lunes, 7=Sábado
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        return $true  # Dentro del horario
    } else {
        return $false # Fuera del horario
    }
}

# Función para activar servicios
function Start-Services {
    Write-Host "🚀 Activando servicios en Railway..." -ForegroundColor Green
    
    # Activar MySQL PRIMERO
    Write-Host "📊 Activando MySQL..." -ForegroundColor Yellow
    $mysqlBody = @{
        query = "mutation { serviceUpdate(id: `"$RAILWAY_MYSQL_ID`", pause: false) { id } }"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "https://backboard.railway.app/graphql/v2" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $RAILWAY_TOKEN"
                "Content-Type" = "application/json"
            } `
            -Body $mysqlBody
    } catch {
        Write-Host "⚠️ Error activando MySQL: $_" -ForegroundColor Yellow
    }
    
    # Esperar 30 segundos para que MySQL esté listo
    Write-Host "⏳ Esperando 30 segundos para que MySQL esté listo..." -ForegroundColor Cyan
    Start-Sleep -Seconds 30
    
    # Activar backend DESPUÉS
    Write-Host "🚀 Activando Backend..." -ForegroundColor Yellow
    $backendBody = @{
        query = "mutation { serviceUpdate(id: `"$RAILWAY_APP_ID`", pause: false) { id } }"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "https://backboard.railway.app/graphql/v2" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $RAILWAY_TOKEN"
                "Content-Type" = "application/json"
            } `
            -Body $backendBody
    } catch {
        Write-Host "⚠️ Error activando Backend: $_" -ForegroundColor Yellow
    }
    
    Write-Host "✅ Ambos servicios activados" -ForegroundColor Green
}

# Función para hibernar servicios
function Stop-Services {
    Write-Host "💤 Hibernando servicios en Railway..." -ForegroundColor Red
    
    # Hibernar backend PRIMERO
    Write-Host " Hibernando Backend..." -ForegroundColor Yellow
    $backendBody = @{
        query = "mutation { serviceUpdate(id: `"$RAILWAY_APP_ID`", pause: true) { id } }"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "https://backboard.railway.app/graphql/v2" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $RAILWAY_TOKEN"
                "Content-Type" = "application/json"
            } `
            -Body $backendBody
    } catch {
        Write-Host "⚠️ Error hibernando Backend: $_" -ForegroundColor Yellow
    }
    
    # Esperar 10 segundos
    Start-Sleep -Seconds 10
    
    # Hibernar MySQL DESPUÉS
    Write-Host "📊 Hibernando MySQL..." -ForegroundColor Yellow
    $mysqlBody = @{
        query = "mutation { serviceUpdate(id: `"$RAILWAY_MYSQL_ID`", pause: true) { id } }"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "https://backboard.railway.app/graphql/v2" `
            -Method POST `
            -Headers @{
                "Authorization" = "Bearer $RAILWAY_TOKEN"
                "Content-Type" = "application/json"
            } `
            -Body $mysqlBody
    } catch {
        Write-Host "⚠️ Error hibernando MySQL: $_" -ForegroundColor Yellow
    }
    
    Write-Host "✅ Ambos servicios hibernados" -ForegroundColor Red
}

# Función para simular diferentes momentos del día
function Test-DifferentTimes {
    Write-Host "🧪 PROBANDO DIFERENTES MOMENTOS DEL DÍA:" -ForegroundColor Magenta
    Write-Host ""
    
    # Simular Lunes 9:00 (antes del horario)
    Write-Host "📅 Lunes 9:00 (Antes del horario):" -ForegroundColor Cyan
    $testDate = Get-Date "2024-08-19 09:00:00"
    $currentHour = $testDate.Hour
    $currentMinute = $testDate.Minute
    $currentTime = $currentHour * 60 + $currentMinute
    $startTime = 10 * 60
    $endTime = 19 * 60
    $currentDay = $testDate.DayOfWeek.value__
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        Write-Host "   ✅ DENTRO del horario - ACTIVARÍA servicios" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FUERA del horario - HIBERNARÍA servicios" -ForegroundColor Red
    }
    
    # Simular Lunes 10:30 (dentro del horario)
    Write-Host "📅 Lunes 10:30 (Dentro del horario):" -ForegroundColor Cyan
    $testDate = Get-Date "2024-08-19 10:30:00"
    $currentHour = $testDate.Hour
    $currentMinute = $testDate.Minute
    $currentTime = $currentHour * 60 + $currentMinute
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        Write-Host "   ✅ DENTRO del horario - ACTIVARÍA servicios" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FUERA del horario - HIBERNARÍA servicios" -ForegroundColor Red
    }
    
    # Simular Lunes 19:30 (después del horario)
    Write-Host "📅 Lunes 19:30 (Después del horario):" -ForegroundColor Cyan
    $testDate = Get-Date "2024-08-19 19:30:00"
    $currentHour = $testDate.Hour
    $currentMinute = $testDate.Minute
    $currentTime = $currentHour * 60 + $currentMinute
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        Write-Host "   ✅ DENTRO del horario - ACTIVARÍA servicios" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FUERA del horario - HIBERNARÍA servicios" -ForegroundColor Red
    }
    
    # Simular Sábado 15:00 (fin de semana)
    Write-Host "📅 Sábado 15:00 (Fin de semana):" -ForegroundColor Cyan
    $testDate = Get-Date "2024-08-17 15:00:00"
    $currentHour = $testDate.Hour
    $currentMinute = $testDate.Minute
    $currentTime = $currentHour * 60 + $currentMinute
    $currentDay = $testDate.DayOfWeek.value__
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        Write-Host "   ✅ DENTRO del horario - ACTIVARÍA servicios" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FUERA del horario - HIBERNARÍA servicios" -ForegroundColor Red
    }
    
    # Simular Domingo 12:00 (fin de semana)
    Write-Host "📅 Domingo 12:00 (Fin de semana):" -ForegroundColor Cyan
    $testDate = Get-Date "2024-08-18 12:00:00"
    $currentHour = $testDate.Hour
    $currentMinute = $testDate.Minute
    $currentTime = $currentHour * 60 + $currentMinute
    $currentDay = $testDate.DayOfWeek.value__
    
    if (($currentDay -ge 2 -and $currentDay -le 6) -and 
        ($currentTime -ge $startTime -and $currentTime -le $endTime)) {
        Write-Host "   ✅ DENTRO del horario - ACTIVARÍA servicios" -ForegroundColor Green
    } else {
        Write-Host "   ❌ FUERA del horario - HIBERNARÍA servicios" -ForegroundColor Red
    }
}

# Lógica principal
Write-Host "🕐 Hora actual: $(Get-Date)" -ForegroundColor White
Write-Host "📅 Día: $(Get-Date -Format 'dddd')" -ForegroundColor White
Write-Host "🌍 Zona horaria: $(Get-Date -Format 'zzz')" -ForegroundColor White
Write-Host ""

# Probar diferentes momentos del día
Test-DifferentTimes

Write-Host ""
Write-Host "🔍 ANÁLISIS DEL HORARIO ACTUAL:" -ForegroundColor Magenta

if (Test-BusinessHours) {
    Write-Host "✅ DENTRO del horario de funcionamiento (10:00-19:00, L-V)" -ForegroundColor Green
    Write-Host "🚀 ACCIÓN: ACTIVARÍA servicios" -ForegroundColor Green
    # Start-Services  # Comentado para testing
} else {
    Write-Host "⏰ FUERA del horario de funcionamiento" -ForegroundColor Red
    Write-Host "💤 ACCIÓN: HIBERNARÍA servicios" -ForegroundColor Red
    # Stop-Services   # Comentado para testing
}

Write-Host ""
Write-Host "💡 Para ejecutar realmente, descomenta las líneas Start-Services/Stop-Services" -ForegroundColor Yellow
