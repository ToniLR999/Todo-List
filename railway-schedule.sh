#!/bin/bash

# Script para gestionar horarios en Railway


RAILWAY_APP_ID="2984dca1-0f61-43b5-a3d3-226ee13fc9d0"
RAILWAY_TOKEN="8b4989af-d29e-4e54-8385-35f3257b33e4"
RAILWAY_PROJECT_ID="8e5843ac-2088-4c98-b09a-7359a4a3c30e"

# Función para verificar si es horario de funcionamiento
is_business_hours() {
    local current_hour=$(date +%H)
    local current_minute=$(date +%M)
    local current_time=$((10#$current_hour * 60 + 10#$current_minute))
    
    # Horario: 10:00 AM - 7:00 PM (Madrid)
    local start_time=$((10 * 60))   # 10:00 AM
    local end_time=$((19 * 60))     # 7:00 PM
    
    # Solo lunes a viernes
    local current_day=$(date +%u)  # 1=Lunes, 7=Domingo
    
    if [ $current_day -ge 1 ] && [ $current_day -le 5 ] && \
       [ $current_time -ge $start_time ] && [ $current_time -le $end_time ]; then
        return 0  # true - dentro del horario
    else
        return 1  # false - fuera del horario
    fi
}

# Función para activar servicios
activate_services() {
    echo "🚀 Activando servicios en Railway..."
    
    # Activar backend
    curl -X POST "https://backboard.railway.app/graphql/v2" \
      -H "Authorization: Bearer $RAILWAY_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"query\":\"mutation { serviceUpdate(id: \\\"$RAILWAY_APP_ID\\\", pause: false) { id } }\"}"
    
    # Activar MySQL (si tienes el service ID)
    # curl -X POST "https://backboard.railway.app/graphql/v2" \
    #   -H "Authorization: Bearer $RAILWAY_TOKEN" \
    #   -H "Content-Type: application/json" \
    #   -d "{\"query\":\"mutation { serviceUpdate(id: \\\"MYSQL_SERVICE_ID\\\", pause: false) { id } }\"}"
    
    echo "✅ Servicios activados"
}

# Función para hibernar servicios
hibernate_services() {
    echo "💤 Hibernando servicios en Railway..."
    
    # Hibernar backend
    curl -X POST "https://backboard.railway.app/graphql/v2" \
      -H "Authorization: Bearer $RAILWAY_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"query\":\"mutation { serviceUpdate(id: \\\"$RAILWAY_APP_ID\\\", pause: true) { id } }\"}"
    
    # Hibernar MySQL (si tienes el service ID)
    # curl -X POST "https://backboard.railway.app/graphql/v2" \
    #   -H "Authorization: Bearer $RAILWAY_TOKEN" \
    #   -H "Content-Type: application/json" \
    #   -d "{\"query\":\"mutation { serviceUpdate(id: \\\"MYSQL_SERVICE_ID\\\", pause: true) { id } }\"}"
    
    echo "✅ Servicios hibernados"
}

# Lógica principal
if is_business_hours; then
    echo "✅ Dentro del horario de funcionamiento (10:00-19:00, L-V)"
    activate_services
else
    echo "⏰ Fuera del horario de funcionamiento"
    hibernate_services
fi

echo "🕐 Hora actual: $(date)"
echo " Día: $(date +%A)"
echo "🌍 Zona horaria: $(date +%Z)"
