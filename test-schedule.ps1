# Script de prueba simple para horarios
Write-Host "PROBANDO LOGICA DE HORARIOS" -ForegroundColor Magenta
Write-Host ""

# Hora actual
$currentHour = (Get-Date).Hour
$currentMinute = (Get-Date).Minute
$currentTime = $currentHour * 60 + $currentMinute
$currentDay = (Get-Date).DayOfWeek.value__

Write-Host "Hora actual: $(Get-Date -Format 'HH:mm')" -ForegroundColor White
Write-Host "Dia: $(Get-Date -Format 'dddd')" -ForegroundColor White
Write-Host "Dia numerico: $currentDay (0=Domingo, 1=Lunes, 6=Sabado)" -ForegroundColor White
Write-Host "Minutos desde medianoche: $currentTime" -ForegroundColor White

# Horarios
$startTime = 10 * 60   # 10:00 AM = 600 minutos
$endTime = 19 * 60     # 7:00 PM = 1140 minutos

Write-Host "Horario inicio: 10:00 ($startTime minutos)" -ForegroundColor Cyan
Write-Host "Horario fin: 19:00 ($endTime minutos)" -ForegroundColor Cyan

# Verificar si es horario de trabajo
# CORREGIDO: 0=Domingo, 1=Lunes, 6=Sabado
$isWorkDay = ($currentDay -ge 1 -and $currentDay -le 5)  # Lunes a Viernes (1-5)
$isWorkTime = ($currentTime -ge $startTime -and $currentTime -le $endTime)
$isBusinessHours = $isWorkDay -and $isWorkTime

Write-Host ""
Write-Host "ANALISIS:" -ForegroundColor Yellow
Write-Host "   Es dia laboral (L-V): $isWorkDay" -ForegroundColor $(if($isWorkDay){"Green"}else{"Red"})
Write-Host "   Es horario laboral (10:00-19:00): $isWorkTime" -ForegroundColor $(if($isWorkTime){"Green"}else{"Red"})
Write-Host "   Esta en horario de funcionamiento: $isBusinessHours" -ForegroundColor $(if($isBusinessHours){"Green"}else{"Red"})

Write-Host ""
if ($isBusinessHours) {
    Write-Host "DENTRO del horario - ACTIVARIA servicios" -ForegroundColor Green
} else {
    Write-Host "FUERA del horario - HIBERNARIA servicios" -ForegroundColor Red
}

# Probar diferentes escenarios
Write-Host ""
Write-Host "ESCENARIOS DE PRUEBA:" -ForegroundColor Magenta

# Lunes 9:00
$testDate = Get-Date "2024-08-19 09:00:00"
$testHour = $testDate.Hour
$testMinute = $testDate.Minute
$testTime = $testHour * 60 + $testMinute
$testDay = $testDate.DayOfWeek.value__
$testWorkDay = ($testDay -ge 1 -and $testDay -le 5)
$testWorkTime = ($testTime -ge $startTime -and $testTime -le $endTime)
$testBusinessHours = $testWorkDay -and $testWorkTime

$action1 = if($testBusinessHours){"ACTIVARIA"}else{"HIBERNARIA"}
Write-Host "Lunes 9:00: $action1" -ForegroundColor $(if($testBusinessHours){"Green"}else{"Red"})

# Lunes 10:30
$testDate = Get-Date "2024-08-19 10:30:00"
$testHour = $testDate.Hour
$testMinute = $testDate.Minute
$testTime = $testHour * 60 + $testMinute
$testDay = $testDate.DayOfWeek.value__
$testWorkDay = ($testDay -ge 1 -and $testDay -le 5)
$testWorkTime = ($testTime -ge $startTime -and $testTime -le $endTime)
$testBusinessHours = $testWorkDay -and $testWorkTime

$action2 = if($testBusinessHours){"ACTIVARIA"}else{"HIBERNARIA"}
Write-Host "Lunes 10:30: $action2" -ForegroundColor $(if($testBusinessHours){"Green"}else{"Red"})

# Sabado 15:00
$testDate = Get-Date "2024-08-17 15:00:00"
$testHour = $testDate.Hour
$testMinute = $testDate.Minute
$testTime = $testHour * 60 + $testMinute
$testDay = $testDate.DayOfWeek.value__
$testWorkDay = ($testDay -ge 1 -and $testDay -le 5)
$testWorkTime = ($testTime -ge $startTime -and $testTime -le $endTime)
$testBusinessHours = $testWorkDay -and $testWorkTime

$action3 = if($testBusinessHours){"ACTIVARIA"}else{"HIBERNARIA"}
Write-Host "Sabado 15:00: $action3" -ForegroundColor $(if($testBusinessHours){"Green"}else{"Red"})

# Domingo 12:00
$testDate = Get-Date "2024-08-18 12:00:00"
$testHour = $testDate.Hour
$testMinute = $testDate.Minute
$testTime = $testHour * 60 + $testMinute
$testDay = $testDate.DayOfWeek.value__
$testWorkDay = ($testDay -ge 1 -and $testDay -le 5)
$testWorkTime = ($testTime -ge $startTime -and $testTime -le $endTime)
$testBusinessHours = $testWorkDay -and $testWorkTime

$action4 = if($testBusinessHours){"ACTIVARIA"}else{"HIBERNARIA"}
Write-Host "Domingo 12:00: $action4" -ForegroundColor $(if($testBusinessHours){"Green"}else{"Red"})
