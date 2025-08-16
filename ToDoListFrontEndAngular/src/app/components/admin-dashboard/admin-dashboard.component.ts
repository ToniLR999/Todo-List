import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ActivatedRoute, Router } from '@angular/router';
import { SystemMetrics } from '../../models/system-metrics.model';
import { AuthService } from '../../services/auth.service';
import * as CryptoJS from 'crypto-js';
import { Subject, interval, takeUntil, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush, // Solo detecta cambios cuando es necesario
})
export class AdminDashboardComponent implements OnInit, OnDestroy, OnChanges {
  metrics: SystemMetrics | null = null;
  isAdmin = false;
  isLoading = false;
  
  // 🚀 OPTIMIZACIONES DE MEMORIA Y CPU
  private destroy$ = new Subject<void>();
  private metricsSubject = new Subject<void>();
  public updateInterval = 120000; // 2 minutos por defecto
  public isHighLoad = false;
  private lastMetricsUpdate = 0;
  public metricsCache = new Map<string, any>();
  private readonly CACHE_TTL = 30000; // 30 segundos de caché
  
  // 🔐 SISTEMA DE SEGURIDAD MULTI-CAPA
  private readonly ADMIN_SECRET = 'ToDoList_Admin_2024_SuperSecret_Key_ChangeInProduction';
  private readonly SALT_ROTATION_INTERVAL = 30 * 60 * 1000; // 30 minutos
  private readonly TOKEN_EXPIRY = 2 * 60 * 60 * 1000; // 2 horas (más restrictivo)
  
  private adminToken: string | null = null;
  private tokenExpiry: number | null = null;
  private currentSalt: string = '';
  private lastSaltRotation: number = 0;
  private failedAttempts: number = 0;
  private lastFailedAttempt: number = 0;
  private readonly MAX_FAILED_ATTEMPTS = 5;
  private readonly LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutos

  constructor(
    private http: HttpClient, 
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef // Añadir esto
  ) {
    this.initializeSecurity();
  }
  
  ngOnInit() {
    this.checkAdminAccess();
    if (this.isAdmin) {
      this.setupOptimizedMetrics();
      this.startAdaptiveMetrics();
      this.startCacheCleanup(); // Iniciar limpieza de caché
    }
  }
  
  ngOnDestroy() {
    // 🧹 LIMPIEZA DE RECURSOS
    this.destroy$.next();
    this.destroy$.complete();
    this.metricsCache.clear();
    console.log('🧹 Recursos del dashboard liberados');
  }

  ngOnChanges(changes: SimpleChanges) {
    // Solo detectar cambios cuando sea necesario
    if (changes['metrics'] && !changes['metrics'].firstChange) {
      this.cdr.detectChanges();
    }
  }

  // 🚀 CONFIGURACIÓN OPTIMIZADA DE MÉTRICAS
  private setupOptimizedMetrics() {
    // Configurar stream de métricas con debouncing
    this.metricsSubject.pipe(
      takeUntil(this.destroy$),
      debounceTime(1000), // Evitar múltiples llamadas en 1 segundo
      distinctUntilChanged(),
      switchMap(() => this.loadMetricsOptimized())
    ).subscribe();
    
    // Cargar métricas iniciales
    this.loadMetricsOptimized();
  }

  // 🚀 MÉTRICAS ADAPTATIVAS INTELIGENTES
  private startAdaptiveMetrics() {
    interval(this.updateInterval).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      // Solo actualizar si no hay alta carga
      if (!this.isHighLoad) {
        this.metricsSubject.next();
      }
    });
  }

  // 🚀 CARGA OPTIMIZADA DE MÉTRICAS CON CACHÉ
  private async loadMetricsOptimized(): Promise<void> {
    const now = Date.now();
    
    // Verificar caché antes de hacer llamadas HTTP
    if (this.metricsCache.has('lastUpdate') && 
        (now - this.metricsCache.get('lastUpdate')) < this.CACHE_TTL) {
      console.log('📦 Usando métricas en caché');
      return;
    }
    
    // Verificar si ya se está cargando
    if (this.isLoading) {
      console.log('⏳ Métricas ya en carga, saltando...');
      return;
    }
    
    console.log('🔄 Iniciando carga optimizada de métricas...');
    this.isLoading = true;
    this.cdr.detectChanges();
    
    try {
      // Cargar métricas con fallbacks optimizados
      await this.loadMetricsWithFallbacks();
      
      // Actualizar caché
      this.metricsCache.set('lastUpdate', now);
      this.lastMetricsUpdate = now;
      
      // Ajustar intervalo basado en la carga del sistema
      this.adjustUpdateInterval();
      
    } catch (error) {
      console.error('❌ Error en carga optimizada:', error);
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  // 🚀 AJUSTE INTELIGENTE DEL INTERVALO DE ACTUALIZACIÓN
  private adjustUpdateInterval() {
    if (!this.metrics) return;
    
    const cpuUsage = this.metrics.cpu.usage;
    const memoryUsage = this.metrics.memory.percentage;
    
    // Si hay alta carga, reducir frecuencia
    if (cpuUsage > 0.7 || memoryUsage > 80) {
      this.updateInterval = 300000; // 5 minutos
      this.isHighLoad = true;
      console.log('⚠️ Alta carga detectada, reduciendo frecuencia de actualización');
    } else if (cpuUsage > 0.4 || memoryUsage > 60) {
      this.updateInterval = 180000; // 3 minutos
      this.isHighLoad = false;
    } else {
      this.updateInterval = 120000; // 2 minutos
      this.isHighLoad = false;
    }
  }

  // 🔐 INICIALIZACIÓN DE SEGURIDAD
  private initializeSecurity() {
    this.currentSalt = this.generateRandomSalt();
    this.lastSaltRotation = Date.now();
    
    // Rotar salt cada 30 minutos
    setInterval(() => this.rotateSalt(), this.SALT_ROTATION_INTERVAL);
  }

  // 🔐 GENERACIÓN DE SALT ALEATORIO
  private generateRandomSalt(): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 32; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }

  // 🔐 ROTACIÓN AUTOMÁTICA DE SALT
  private rotateSalt() {
    this.currentSalt = this.generateRandomSalt();
    this.lastSaltRotation = Date.now();
    this.adminToken = null; // Invalidar token anterior
    console.log('🔄 Salt rotado por seguridad');
  }

  // 🔐 VERIFICACIÓN DE INTENTOS FALLIDOS
  private checkFailedAttempts(): boolean {
    const now = Date.now();
    
    // Resetear contador si han pasado 15 minutos
    if (now - this.lastFailedAttempt > this.LOCKOUT_DURATION) {
      this.failedAttempts = 0;
      return false;
    }
    
    // Verificar si está bloqueado
    if (this.failedAttempts >= this.MAX_FAILED_ATTEMPTS) {
      console.error('🚫 Cuenta bloqueada por múltiples intentos fallidos');
      return true;
    }
    
    return false;
  }

  // 🔐 REGISTRAR INTENTO FALLIDO
  private recordFailedAttempt() {
    this.failedAttempts++;
    this.lastFailedAttempt = Date.now();
    console.warn(`⚠️ Intento fallido ${this.failedAttempts}/${this.MAX_FAILED_ATTEMPTS}`);
    
    if (this.failedAttempts >= this.MAX_FAILED_ATTEMPTS) {
      console.error('🚫 Cuenta bloqueada por 15 minutos');
    }
  }

  // 🔐 GENERACIÓN DE TOKEN JWT SEGURO
  private generateSecureAdminToken(): string {
    const now = Date.now();
    const header = btoa(JSON.stringify({ 
      alg: 'HS256', 
      typ: 'JWT',
      kid: this.generateKeyId() // Key ID para rotación
    }));
    
    const payload = btoa(JSON.stringify({
      role: 'admin',
      exp: Math.floor((now + this.TOKEN_EXPIRY) / 1000),
      iat: Math.floor(now / 1000),
      nbf: Math.floor(now / 1000), // Not Before
      sub: 'admin-dashboard',
      aud: 'admin-users', // Audience
      iss: 'ToDoList-Admin', // Issuer
      jti: this.generateJWTId(), // JWT ID único
      salt: this.currentSalt,
      version: '2.0'
    }));
    
    const signature = this.generateSecureSignature(header + '.' + payload);
    
    return `${header}.${payload}.${signature}`;
  }

  // 🔐 GENERACIÓN DE KEY ID
  private generateKeyId(): string {
    return CryptoJS.SHA256(this.ADMIN_SECRET + this.currentSalt).toString().substring(0, 16);
  }

  // 🔐 GENERACIÓN DE JWT ID ÚNICO
  private generateJWTId(): string {
    return CryptoJS.SHA256(Date.now().toString() + this.currentSalt + Math.random().toString()).toString().substring(0, 16);
  }

  // 🔐 GENERACIÓN DE FIRMA SEGURA
  private generateSecureSignature(data: string): string {
    const hmac = CryptoJS.HmacSHA256(data, this.ADMIN_SECRET + this.currentSalt);
    return hmac.toString(CryptoJS.enc.Base64url);
  }

  // 🔐 VALIDACIÓN COMPLETA DEL TOKEN
  private validateToken(token: string): boolean {
    try {
      // Verificar formato JWT
      if (!token || token.split('.').length !== 3) {
        return false;
      }

      const parts = token.split('.');
      const header = JSON.parse(atob(parts[0]));
      const payload = JSON.parse(atob(parts[1]));
      const signature = parts[2];

      // Verificar algoritmo
      if (header.alg !== 'HS256') {
        return false;
      }

      // Verificar expiración
      if (payload.exp < Math.floor(Date.now() / 1000)) {
        console.warn('⚠️ Token expirado');
        return false;
      }

      // Verificar Not Before
      if (payload.nbf > Math.floor(Date.now() / 1000)) {
        console.warn('⚠️ Token no válido aún');
        return false;
      }

      // Verificar salt
      if (payload.salt !== this.currentSalt) {
        console.warn('⚠️ Salt inválido - posible token antiguo');
        return false;
      }

      // Verificar firma
      const expectedSignature = this.generateSecureSignature(parts[0] + '.' + parts[1]);
      if (signature !== expectedSignature) {
        console.warn('⚠️ Firma inválida');
        return false;
      }

      // Verificar versión
      if (payload.version !== '2.0') {
        console.warn('⚠️ Versión de token obsoleta');
        return false;
      }

      return true;
    } catch (error) {
      console.error('❌ Error validando token:', error);
      return false;
    }
  }

  // 🔐 OBTENER TOKEN VÁLIDO
  private getValidAdminToken(): string {
    if (!this.isTokenValid()) {
      this.adminToken = this.generateSecureAdminToken();
      this.tokenExpiry = Date.now() + this.TOKEN_EXPIRY;
      console.log('🔄 Nuevo token admin generado, expira en:', new Date(this.tokenExpiry).toLocaleString());
    }
    return this.adminToken || '';
  }

  // 🔐 VERIFICAR VALIDEZ DEL TOKEN
  private isTokenValid(): boolean {
    if (!this.adminToken || !this.tokenExpiry) {
      return false;
    }
    return Date.now() < this.tokenExpiry;
  }

  // 🔐 VERIFICAR ACCESO ADMIN
  private checkAdminAccess() {
    if (!this.authService.isAuthenticated()) {
      this.isAdmin = false;
      this.router.navigate(['/login'], { replaceUrl: true });
      return;
    }
    
    // Verificar bloqueo por intentos fallidos
    if (this.checkFailedAttempts()) {
      this.isAdmin = false;
      const remainingTime = Math.ceil((this.LOCKOUT_DURATION - (Date.now() - this.lastFailedAttempt)) / 60000);
      alert(`🚫 Cuenta bloqueada por múltiples intentos fallidos. Intenta de nuevo en ${remainingTime} minutos.`);
      this.router.navigate(['/login'], { replaceUrl: true });
      return;
    }
    
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      
      if (token && this.validateToken(token)) {
        this.isAdmin = true;
        this.failedAttempts = 0; // Resetear contador de intentos fallidos
        console.log('✅ Acceso admin autorizado');
      } else if (!environment.production) {
        this.isAdmin = true;
        console.warn('⚠️ Acceso admin en modo desarrollo (sin token)');
      } else {
        this.isAdmin = false;
        this.recordFailedAttempt();
        console.error('❌ Token de admin inválido o expirado');
        this.router.navigate(['/login'], { replaceUrl: true });
      }
    });
  }

  // 🔐 MÉTODOS PÚBLICOS DE SEGURIDAD
  public getAdminUrl(): string {
    const token = this.getValidAdminToken();
    return `${window.location.origin}/admin?token=${token}`;
  }

  public checkTokenStatus(): { 
    isValid: boolean; 
    expiresAt: string | null; 
    saltAge: string | null;
    failedAttempts: number;
    isLocked: boolean;
  } {
    return {
      isValid: this.isTokenValid(),
      expiresAt: this.tokenExpiry ? new Date(this.tokenExpiry).toLocaleString() : null,
      saltAge: this.lastSaltRotation ? new Date(this.lastSaltRotation).toLocaleString() : null,
      failedAttempts: this.failedAttempts,
      isLocked: this.checkFailedAttempts()
    };
  }

  public forceTokenRotation() {
    this.rotateSalt();
    this.adminToken = null;
    console.log('🔄 Rotación forzada de token completada');
  }
  
  loadMetrics() {
    console.log('🔄 Iniciando carga de métricas...');
    this.isLoading = true;
    this.cdr.detectChanges();
    
    // Cargar métricas reales usando los endpoints que funcionan
    Promise.all([
      this.http.get(`${environment.apiUrl}/actuator/health`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/jvm.memory.used`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/jvm.memory.max`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/process.cpu.usage`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/hikaricp.connections.active`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/hikaricp.connections.max`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/process.uptime`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/system.cpu.count`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/system.cpu.usage`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/disk.free`),
      this.http.get(`${environment.apiUrl}/actuator/metrics/disk.total`)
    ]).then(([health, memoryUsed, memoryMax, cpuUsage, dbActive, dbMax, uptime, cpuCount, systemCpu, diskFree, diskTotal]: any[]) => {
      
      console.log('📊 Datos recibidos:', {
        health, memoryUsed, memoryMax, cpuUsage, 
        dbActive, dbMax, uptime, cpuCount, systemCpu, diskFree, diskTotal
      });
      
      // Calcular métricas de memoria (convertir de bytes a MB para mejor legibilidad)
      const usedMemoryBytes = memoryUsed?.measurements?.[0]?.value || 0;
      const maxMemoryBytes = memoryMax?.measurements?.[0]?.value || 0;
      const usedMemoryMB = Math.round(usedMemoryBytes / (1024 * 1024));
      const maxMemoryMB = Math.round(maxMemoryBytes / (1024 * 1024));
      const memoryPercentage = maxMemoryMB > 0 ? (usedMemoryMB / maxMemoryMB) * 100 : 0;
      
      // CPU usage - usar system.cpu.usage si está disponible, sino process.cpu.usage
      let cpuUsageValue = 0;
      if (systemCpu?.measurements?.[0]?.value !== undefined) {
        cpuUsageValue = systemCpu.measurements[0].value;
      } else if (cpuUsage?.measurements?.[0]?.value !== undefined) {
        cpuUsageValue = cpuUsage.measurements[0].value;
      }
      
      // Database connections
      const dbConnections = dbActive?.measurements?.[0]?.value || 0;
      const dbMaxConnections = dbMax?.measurements?.[0]?.value || 0;
      
      // Uptime en segundos
      const uptimeSeconds = uptime?.measurements?.[0]?.value || 0;
      
      // CPU count
      const cpuCountValue = cpuCount?.measurements?.[0]?.value || 1;
      
      // Disk metrics
      const diskFreeBytes = diskFree?.measurements?.[0]?.value || 0;
      const diskTotalBytes = diskTotal?.measurements?.[0]?.value || 0;
      const diskUsedBytes = diskTotalBytes - diskFreeBytes;
      const diskUsagePercentage = diskTotalBytes > 0 ? (diskUsedBytes / diskTotalBytes) * 100 : 0;
      
      this.metrics = {
        memory: { 
          used: usedMemoryMB, 
          max: maxMemoryMB,   
          percentage: Math.round(memoryPercentage * 100) / 100 
        },
        cpu: { 
          usage: cpuUsageValue, 
          load: Math.round(cpuUsageValue * 100 * 100) / 100 
        },
        database: { 
          connections: dbConnections, 
          maxConnections: dbMaxConnections, 
          status: health?.components?.db?.status || 'UNKNOWN'
        },
        redis: { 
          status: health?.components?.redis?.status || 'UNKNOWN',
          operations: 0
        },
        app: { 
          status: health?.status || 'UNKNOWN',
          uptime: uptimeSeconds, 
          schedule: this.getScheduleInfo(),
          version: '1.0.0' // Por ahora hardcodeado
        }
      };
      
      console.log('✅ Métricas procesadas:', this.metrics);
      this.isLoading = false;
      
      // Forzar detección de cambios manualmente
      this.cdr.detectChanges();
      
    }).catch(error => {
      console.error('❌ Error cargando métricas:', error);
      this.isLoading = false;
      // En caso de error, mostrar solo health básico
      this.loadBasicHealth();
      this.cdr.detectChanges();
    });
  }

  // Método para manejar errores de métricas específicas
  private handleMetricError(metricName: string, error: any): any {
    console.warn(`⚠️ Error cargando métrica ${metricName}:`, error);
    return {
      measurements: [{ value: 0 }],
      available: false,
      error: error.message || 'Error desconocido'
    };
  }

  // Método para cargar métricas con manejo de errores individual
  private loadMetricWithFallback(endpoint: string, metricName: string): Promise<any> {
    return this.http.get(endpoint).toPromise().catch(error => {
      return this.handleMetricError(metricName, error);
    });
  }

  // Método mejorado para cargar métricas con fallbacks
  async loadMetricsWithFallbacks() {
    console.log('🔄 Iniciando carga de métricas con fallbacks...');
    this.isLoading = true;
    this.cdr.detectChanges();

    try {
      // Cargar health primero como base
      const health = await this.loadMetricWithFallback(
        `${environment.apiUrl}/actuator/health`, 
        'health'
      );

      // Cargar métricas específicas con fallbacks
      const [
        memoryUsed, memoryMax, cpuUsage, dbActive, dbMax, 
        uptime, cpuCount, systemCpu, diskFree, diskTotal
      ] = await Promise.all([
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/jvm.memory.used`, 'memory.used'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/jvm.memory.max`, 'memory.max'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/process.cpu.usage`, 'cpu.usage'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/hikaricp.connections.active`, 'db.connections.active'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/hikaricp.connections.max`, 'db.connections.max'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/process.uptime`, 'uptime'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/system.cpu.count`, 'cpu.count'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/system.cpu.usage`, 'system.cpu.usage'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/disk.free`, 'disk.free'),
        this.loadMetricWithFallback(`${environment.apiUrl}/actuator/metrics/disk.total`, 'disk.total')
      ]);

      console.log('📊 Datos recibidos con fallbacks:', {
        health, memoryUsed, memoryMax, cpuUsage, 
        dbActive, dbMax, uptime, cpuCount, systemCpu, diskFree, diskTotal
      });

      // Procesar métricas con validación
      this.processMetrics(health, memoryUsed, memoryMax, cpuUsage, dbActive, dbMax, uptime, cpuCount, systemCpu, diskFree, diskTotal);

    } catch (error) {
      console.error('❌ Error general cargando métricas:', error);
      this.loadBasicHealth();
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  // Método para procesar métricas extraídas
  private processMetrics(health: any, memoryUsed: any, memoryMax: any, cpuUsage: any, 
                        dbActive: any, dbMax: any, uptime: any, cpuCount: any, 
                        systemCpu: any, diskFree: any, diskTotal: any) {
    
    console.log('🔍 Procesando métricas individuales:', {
      memoryUsed, memoryMax, cpuUsage, dbActive, dbMax, uptime, systemCpu
    });
    
    // Memoria - convertir bytes a MB
    const usedMemoryBytes = memoryUsed?.measurements?.[0]?.value || 0;
    const maxMemoryBytes = memoryMax?.measurements?.[0]?.value || 0;
    const usedMemoryMB = Math.round(usedMemoryBytes / (1024 * 1024));
    const maxMemoryMB = Math.round(maxMemoryBytes / (1024 * 1024));
    const memoryPercentage = maxMemoryMB > 0 ? (usedMemoryMB / maxMemoryMB) * 100 : 0;

    // CPU - priorizar system.cpu.usage
    let cpuUsageValue = 0;
    if (systemCpu?.measurements?.[0]?.value !== undefined && !systemCpu.error) {
      cpuUsageValue = systemCpu.measurements[0].value;
    } else if (cpuUsage?.measurements?.[0]?.value !== undefined && !cpuUsage.error) {
      cpuUsageValue = cpuUsage.measurements[0].value;
    }

    // Base de datos - manejar array de valores si es necesario
    let dbConnections = 0;
    let dbMaxConnections = 0;
    
    if (dbActive?.measurements?.[0]?.value !== undefined) {
      const value = dbActive.measurements[0].value;
      dbConnections = Array.isArray(value) ? value[0] : value;
    }
    
    if (dbMax?.measurements?.[0]?.value !== undefined) {
      const value = dbMax.measurements[0].value;
      dbMaxConnections = Array.isArray(value) ? value[0] : value;
    }

    // Uptime
    const uptimeSeconds = uptime?.measurements?.[0]?.value || 0;

    // Disco
    const diskFreeBytes = diskFree?.measurements?.[0]?.value || 0;
    const diskTotalBytes = diskTotal?.measurements?.[0]?.value || 0;
    const diskUsedBytes = diskTotalBytes - diskFreeBytes;
    const diskUsagePercentage = diskTotalBytes > 0 ? (diskUsedBytes / diskTotalBytes) * 100 : 0;

    this.metrics = {
      memory: { 
        used: usedMemoryMB, 
        max: maxMemoryMB,   
        percentage: Math.round(memoryPercentage * 100) / 100 
      },
      cpu: { 
        usage: cpuUsageValue, 
        load: Math.round(cpuUsageValue * 100 * 100) / 100 
      },
      database: { 
        connections: dbConnections, 
        maxConnections: dbMaxConnections, 
        status: health?.components?.db?.status || 'UNKNOWN'
      },
      redis: { 
        status: health?.components?.redis?.status || 'UNKNOWN',
        operations: 0
      },
      app: { 
        status: health?.status || 'UNKNOWN',
        uptime: uptimeSeconds, 
        schedule: this.getScheduleInfo(),
        version: '1.0.0'
      }
    };

    console.log('✅ Métricas procesadas con fallbacks:', this.metrics);
  }

  // Método auxiliar para obtener información del horario
  private getScheduleInfo(): string {
    const now = new Date();
    const hour = now.getHours();
    
    if (hour >= 6 && hour < 12) return 'Mañana (6:00-12:00)';
    if (hour >= 12 && hour < 18) return 'Tarde (12:00-18:00)';
    if (hour >= 18 && hour < 22) return 'Noche (18:00-22:00)';
    return 'Madrugada (22:00-6:00)';
  }

  private loadBasicHealth() {
    this.http.get(`${environment.apiUrl}/actuator/health`).subscribe({
      next: (health: any) => {
        console.log('📊 Health endpoint response:', health);
        
        // Extraer información útil del health endpoint
        const dbStatus = health?.components?.db?.status || 'UNKNOWN';
        const redisStatus = health?.components?.redis?.status || 'UNKNOWN';
        const diskSpace = health?.components?.diskSpace;
        const mailStatus = health?.components?.mail?.status || 'UNKNOWN';
        
        // Calcular espacio en disco si está disponible
        let diskUsage = 0;
        if (diskSpace?.details) {
          const total = diskSpace.details.total || 0;
          const free = diskSpace.details.free || 0;
          const used = total - free;
          diskUsage = total > 0 ? (used / total) * 100 : 0;
        }
        
        this.metrics = {
          memory: { 
            used: 0, 
            max: 0, 
            percentage: 0 
          },
          cpu: { 
            usage: 0, 
            load: 0 
          },
          database: { 
            connections: 0, 
            maxConnections: 0, 
            status: dbStatus
          },
          redis: { 
            status: redisStatus, 
            operations: 0
          },
          app: { 
            status: health?.status || 'UNKNOWN',
            uptime: 0,
            schedule: this.getScheduleInfo(),
            version: '1.0.0'
          }
        };
        
        console.log('✅ Métricas básicas cargadas desde health endpoint:', this.metrics);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ Error cargando health básico:', error);
        // Métricas por defecto en caso de error total
        this.metrics = {
          memory: { used: 0, max: 0, percentage: 0 },
          cpu: { usage: 0, load: 0 },
          database: { connections: 0, maxConnections: 0, status: 'ERROR' },
          redis: { status: 'ERROR', operations: 0 },
          app: { 
            status: 'ERROR',
            uptime: 0,
            schedule: 'N/A',
            version: 'N/A'
          }
        };
        this.cdr.detectChanges();
      }
    });
  }
  
  refreshMetrics() {
    // Limpiar caché y forzar actualización
    this.metricsCache.clear();
    this.metricsSubject.next();
  }
  
  clearCache() {
    this.http.post(`${environment.apiUrl}/api/admin/clear-cache`, {}).subscribe({
      next: () => {
        alert('Caché limpiado correctamente');
        // Limpiar caché local y forzar actualización
        this.metricsCache.clear();
        this.metricsSubject.next();
      },
      error: (error) => {
        alert('Error limpiando caché: ' + error.message);
      }
    });
  }
  
  exportMetrics() {
    const report = {
      timestamp: new Date().toISOString(),
      metrics: this.metrics
    };
    
    const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `metrics-report-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
  
  getLastUpdate(): string {
    return new Date().toLocaleString('es-ES');
  }
  
  formatBytes(bytes: number): string {
    if (!bytes) return 'N/A';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
  }
  
  formatUptime(seconds: number): string {
    if (!seconds) return 'N/A';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours}h ${minutes}m`;
  }
  
  getMemoryClass(percentage: number): string {
    if (!percentage) return '';
    if (percentage < 70) return 'memory-ok';
    if (percentage < 90) return 'memory-warning';
    return 'memory-danger';
  }
  
  getCpuClass(usage: number): string {
    if (!usage) return '';
    const percentage = usage * 100;
    if (percentage < 70) return 'cpu-ok';
    if (percentage < 90) return 'cpu-warning';
    return 'cpu-danger';
  }
  
  getDbClass(status: string): string {
    return status === 'UP' ? 'status-up' : 'status-down';
  }
  
  getRedisClass(status: string): string {
    return status === 'UP' ? 'status-up' : 'status-down';
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  // Método para mostrar el estado de optimización
  public getOptimizationStatus() {
    return {
      currentInterval: this.updateInterval / 1000 + ' segundos',
      isHighLoad: this.isHighLoad,
      recommendedActions: this.getRecommendedActions(),
      cacheSize: this.metricsCache.size,
      lastUpdate: this.lastMetricsUpdate ? new Date(this.lastMetricsUpdate).toLocaleString() : 'Nunca'
    };
  }

  private getRecommendedActions(): string[] {
    const actions = [];
    
    if (this.metrics) {
      if (this.metrics.cpu.usage > 0.7) {
        actions.push('Reducir frecuencia de métricas');
        actions.push('Deshabilitar métricas no esenciales');
      }
      
      if (this.metrics.memory.percentage > 80) {
        actions.push('Reducir pool de conexiones');
        actions.push('Optimizar JVM settings');
      }
    }
    
    return actions.length > 0 ? actions : ['Sistema optimizado'];
  }

  // 🚀 LIMPIEZA PERIÓDICA DE CACHÉ
  private startCacheCleanup() {
    interval(60000).pipe( // Cada minuto
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.cleanupCache();
    });
  }

  private cleanupCache() {
    const now = Date.now();
    const keysToDelete: string[] = [];
    
    this.metricsCache.forEach((value, key) => {
      if (key !== 'lastUpdate' && (now - value.timestamp) > this.CACHE_TTL) {
        keysToDelete.push(key);
      }
    });
    
    keysToDelete.forEach(key => this.metricsCache.delete(key));
    
    if (keysToDelete.length > 0) {
      console.log(`🧹 Caché limpiado: ${keysToDelete.length} entradas eliminadas`);
    }
  }

  // 🚀 ESTADÍSTICAS DE RENDIMIENTO
  public getPerformanceStats() {
    return {
      cacheSize: this.metricsCache.size,
      cacheHitRate: this.calculateCacheHitRate(),
      updateInterval: this.updateInterval / 1000,
      isHighLoad: this.isHighLoad,
      lastUpdate: this.lastMetricsUpdate ? new Date(this.lastMetricsUpdate).toLocaleString() : 'Nunca',
      memoryUsage: this.metrics?.memory?.percentage || 0,
      cpuUsage: this.metrics?.cpu?.usage || 0
    };
  }

  private calculateCacheHitRate(): number {
    // Implementación simple de tasa de aciertos de caché
    const totalRequests = this.metricsCache.get('totalRequests') || 0;
    const cacheHits = this.metricsCache.get('cacheHits') || 0;
    
    if (totalRequests === 0) return 0;
    return Math.round((cacheHits / totalRequests) * 100);
  }

  // 🚀 MOSTRAR ESTADÍSTICAS DE RENDIMIENTO
  public showPerformanceStats() {
    const stats = this.getPerformanceStats();
    const message = `
🚀 Estadísticas de Rendimiento:

📦 Tamaño del Caché: ${stats.cacheSize} entradas
🎯 Tasa de Aciertos: ${stats.cacheHitRate}%
⏱️ Intervalo de Actualización: ${stats.updateInterval} segundos
⚠️ Alta Carga: ${stats.isHighLoad ? 'Sí' : 'No'}
🔄 Última Actualización: ${stats.lastUpdate}
💾 Uso de Memoria: ${stats.memoryUsage.toFixed(1)}%
⚡ Uso de CPU: ${(stats.cpuUsage * 100).toFixed(1)}%

Para más detalles, revisa la consola del navegador.
    `;
    
    alert(message);
  }

  // Método para obtener información de diagnóstico
  public getDiagnosticInfo(): any {
    return {
      timestamp: new Date().toISOString(),
      environment: environment.production ? 'Production' : 'Development',
      apiUrl: environment.apiUrl,
      isAdmin: this.isAdmin,
      metricsLoaded: !!this.metrics,
      lastUpdate: this.getLastUpdate(),
      tokenStatus: this.checkTokenStatus(),
      optimizationStatus: this.getOptimizationStatus()
    };
  }

  // Método para mostrar alerta de diagnóstico
  public showDiagnosticAlert() {
    const diagnostic = this.getDiagnosticInfo();
    const message = `
🔍 Información de Diagnóstico:

📅 Timestamp: ${diagnostic.timestamp}
🌍 Entorno: ${diagnostic.environment}
🔗 API URL: ${diagnostic.apiUrl}
👤 Es Admin: ${diagnostic.isAdmin}
📊 Métricas Cargadas: ${diagnostic.metricsLoaded}
🔄 Última Actualización: ${diagnostic.lastUpdate}
🔐 Estado del Token: ${diagnostic.tokenStatus.isValid ? 'Válido' : 'Inválido'}
⚡ Estado de Optimización: ${diagnostic.optimizationStatus.currentInterval}

Para más detalles, revisa la consola del navegador.
    `;
    
    alert(message);
  }

  // Método para probar endpoints individuales
  public async testEndpoints() {
    console.log('🧪 Probando endpoints individuales...');
    
    const endpoints = [
      { name: 'Health', url: `${environment.apiUrl}/actuator/health` },
      { name: 'Memory Used', url: `${environment.apiUrl}/actuator/metrics/jvm.memory.used` },
      { name: 'Memory Max', url: `${environment.apiUrl}/actuator/metrics/jvm.memory.max` },
      { name: 'CPU Usage', url: `${environment.apiUrl}/actuator/metrics/process.cpu.usage` },
      { name: 'System CPU', url: `${environment.apiUrl}/actuator/metrics/system.cpu.usage` },
      { name: 'DB Active', url: `${environment.apiUrl}/actuator/metrics/hikaricp.connections.active` },
      { name: 'DB Max', url: `${environment.apiUrl}/actuator/metrics/hikaricp.connections.max` },
      { name: 'Uptime', url: `${environment.apiUrl}/actuator/metrics/process.uptime` }
    ];

    const results = [];
    
    for (const endpoint of endpoints) {
      try {
        const response = await this.http.get(endpoint.url).toPromise();
        results.push({
          name: endpoint.name,
          status: '✅ OK',
          data: response
        });
        console.log(`✅ ${endpoint.name}:`, response);
      } catch (error: any) {
        results.push({
          name: endpoint.name,
          status: '❌ ERROR',
          error: error.message || 'Error desconocido'
        });
        console.error(`❌ ${endpoint.name}:`, error);
      }
    }

    // Mostrar resumen
    const summary = results.map(r => `${r.name}: ${r.status}`).join('\n');
    alert(`🧪 Resultados de Prueba de Endpoints:\n\n${summary}\n\nRevisa la consola para más detalles.`);
    
    return results;
  }
}
