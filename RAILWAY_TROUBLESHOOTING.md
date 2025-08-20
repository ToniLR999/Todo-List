# 🚂 Railway Troubleshooting Guide

## 🔍 **Problema Actual:**
El GitHub Action `railway-scheduler` está fallando con el error:
```
{"errors":[{"message":"Problem processing request","traceId":"7396507411545153570"}]}
```

## 🛠️ **Soluciones Implementadas:**

### 1. **Workflow de Diagnóstico**
Se ha creado un nuevo workflow `.github/workflows/railway-diagnostic.yml` que puedes ejecutar manualmente para diagnosticar problemas.

**Cómo usar:**
1. Ve a GitHub Actions
2. Ejecuta "Railway Diagnostic" manualmente
3. Revisa los logs para identificar el problema específico

### 2. **Mejoras en el Scheduler Principal**
El workflow principal ahora incluye:
- ✅ Verificación de conectividad
- ✅ Validación del token
- ✅ Validación del service ID
- ✅ Logging detallado
- ✅ Manejo robusto de errores

## 🔧 **Pasos para Solucionar:**

### **Paso 1: Ejecutar Diagnóstico**
```bash
# En GitHub Actions, ejecuta "Railway Diagnostic"
# Esto te dará información detallada sobre:
# - Estado del token
# - Validez del service ID
# - Permisos disponibles
```

### **Paso 2: Verificar Secretos**
Asegúrate de que estos secretos estén configurados en tu repositorio:

1. **RAILWAY_TOKEN**: Token de autenticación de Railway
2. **RAILWAY_SERVICE_ID**: ID del servicio en Railway

**Ubicación:** Settings → Secrets and variables → Actions

### **Paso 3: Generar Nuevo Token**
Si el token está expirado:

1. Ve a [Railway Dashboard](https://railway.app/dashboard)
2. Settings → Tokens
3. Generate new token
4. Copia el token y actualiza el secreto en GitHub

### **Paso 4: Obtener Service ID Correcto**
Para obtener el service ID:

1. Ve a tu proyecto en Railway
2. Selecciona el servicio
3. En la URL verás: `https://railway.app/project/[PROJECT_ID]/service/[SERVICE_ID]`
4. Copia el SERVICE_ID

### **Paso 5: Verificar Permisos**
El token debe tener permisos para:
- ✅ Leer información del usuario
- ✅ Leer información del servicio
- ✅ Modificar el estado del servicio (pause/unpause)

## 🧪 **Modo de Prueba:**

El scheduler principal ahora incluye un modo de prueba:

1. Ejecuta "Railway Scheduler" manualmente
2. Marca "test_mode" como `true`
3. Esto ejecutará el script sin modificar Railway

## 📋 **Comandos de Debug:**

### **Verificar Conectividad:**
```bash
curl -s https://backboard.railway.app/graphql/v2
```

### **Verificar Token:**
```bash
curl -X POST https://backboard.railway.app/graphql/v2 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "query { viewer { id } }"}'
```

### **Verificar Service:**
```bash
curl -X POST https://backboard.railway.app/graphql/v2 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "query { service(id: \"SERVICE_ID\") { id name } }"}'
```

## 🚨 **Errores Comunes:**

### **"Problem processing request"**
- **Causa**: Token expirado o inválido
- **Solución**: Regenerar token en Railway

### **"Service not found"**
- **Causa**: Service ID incorrecto
- **Solución**: Verificar ID en Railway dashboard

### **"Unauthorized"**
- **Causa**: Token sin permisos suficientes
- **Solución**: Verificar permisos del token

### **"Rate limit exceeded"**
- **Causa**: Demasiadas requests
- **Solución**: Esperar o reducir frecuencia del cron

## 📅 **Configuración del Cron:**

El scheduler se ejecuta cada 15 minutos:
```yaml
cron: "*/15 * * * *"   # cada 15 min (UTC)
```

**Nota:** Los horarios están en UTC, pero el script usa Europe/Madrid para los cálculos.

## 🔄 **Flujo de Trabajo:**

1. **Cada 15 minutos**: GitHub Actions ejecuta el scheduler
2. **Verificación**: Conectividad, token, service ID
3. **Cálculo**: Hora actual vs horario de negocio
4. **Acción**: Pause/unpause según corresponda
5. **Logging**: Información detallada del proceso

## 📞 **Soporte:**

Si el problema persiste:

1. Ejecuta el workflow de diagnóstico
2. Revisa los logs completos
3. Verifica la configuración de secretos
4. Comprueba los permisos del token

## 🎯 **Estado Actual:**

- ✅ **Script mejorado** con validaciones
- ✅ **Workflow de diagnóstico** disponible
- ✅ **Logging detallado** implementado
- ✅ **Manejo robusto** de errores
- ✅ **Modo de prueba** disponible

---

**Última actualización:** $(date)
**Versión:** 2.0.0
