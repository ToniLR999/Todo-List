import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface SystemMetrics {
  memory: {
    used: number;
    max: number;
    percentage: number;
  };
  cpu: {
    usage: number;
    load: number;
    cores: number;
  };
  database: {
    connections: number;
    maxConnections: number;
    status: string;
  };
  redis: {
    status: string;
    operations: number;
  };
  app: {
    status: string;
    uptime: number;
    schedule: string;
    version: string;
  };
  disk: {
    used: number;
    total: number;
    percentage: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class SystemMetricsService {
  private metricsSubject = new BehaviorSubject<SystemMetrics | null>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  
  public metrics$ = this.metricsSubject.asObservable();
  public isLoading$ = this.isLoadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();

  // Configuración de métricas
  private readonly UPDATE_INTERVAL = 30000;
  private readonly CPU_THRESHOLD = 0.95;
  private readonly MEMORY_THRESHOLD = 0.9;
  private readonly DISK_THRESHOLD = 0.85;

  constructor(private http: HttpClient) {
    this.startMetricsMonitoring();
  }

  /**
   * Inicia el monitoreo automático de métricas
   */
  private startMetricsMonitoring(): void {
    interval(this.UPDATE_INTERVAL).pipe(
      switchMap(() => this.loadMetrics())
    ).subscribe();
  }

  /**
   * Carga las métricas del sistema con validaciones robustas
   */
  public loadMetrics(): Observable<SystemMetrics> {
    this.isLoadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get(`${environment.apiUrl}/actuator/health`).pipe(
      switchMap(health => this.loadDetailedMetrics(health)),
      map(metrics => this.validateAndSanitizeMetrics(metrics)),
      tap(metrics => {
        this.metricsSubject.next(metrics);
        this.isLoadingSubject.next(false);
      }),
      catchError(error => {
        console.error('❌ Error cargando métricas:', error);
        this.errorSubject.next('Error cargando métricas del sistema');
        this.isLoadingSubject.next(false);
        
        // Retornar métricas básicas en caso de error
        const fallbackMetrics = this.getFallbackMetrics();
        this.metricsSubject.next(fallbackMetrics);
        return of(fallbackMetrics);
      })
    );
  }

  /**
   * Carga métricas detalladas desde múltiples endpoints
   */
  private loadDetailedMetrics(health: any): Observable<SystemMetrics> {
    const endpoints = [
      'jvm.memory.used',
      'jvm.memory.max',
      'process.cpu.usage',
      'system.cpu.usage',
      'system.cpu.count',
      'hikaricp.connections.active',
      'hikaricp.connections.max',
      'process.uptime',
      'disk.free',
      'disk.total'
    ];

    const requests = endpoints.map(endpoint => 
      this.http.get(`${environment.apiUrl}/actuator/metrics/${endpoint}`).pipe(
        catchError(() => of(null))
      )
    );

    return new Observable(observer => {
      Promise.all(requests).then(responses => {
        const [
          memoryUsed, memoryMax, processCpu, systemCpu, cpuCount,
          dbActive, dbMax, uptime, diskFree, diskTotal
        ] = responses;

        const metrics = this.processMetricsData(
          health, memoryUsed, memoryMax, processCpu, systemCpu, 
          cpuCount, dbActive, dbMax, uptime, diskFree, diskTotal
        );

        observer.next(metrics);
        observer.complete();
      }).catch(error => {
        observer.error(error);
      });
    });
  }

  /**
   * Procesa los datos de métricas con validaciones
   */
  private processMetricsData(
    health: any, memoryUsed: any, memoryMax: any, processCpu: any,
    systemCpu: any, cpuCount: any, dbActive: any, dbMax: any,
    uptime: any, diskFree: any, diskTotal: any
  ): SystemMetrics {
    
    // Memoria con validación
    const usedMemoryBytes = this.extractMetricValue(memoryUsed) || 0;
    const maxMemoryBytes = this.extractMetricValue(memoryMax) || 0;
    const usedMemoryMB = Math.round(usedMemoryBytes / (1024 * 1024));
    const maxMemoryMB = Math.round(maxMemoryBytes / (1024 * 1024));
    const memoryPercentage = maxMemoryMB > 0 ? Math.min(100, (usedMemoryMB / maxMemoryMB) * 100) : 0;

    // CPU con validación robusta
    const cpuUsageValue = this.validateCpuUsage(systemCpu, processCpu);
    const cpuCores = this.extractMetricValue(cpuCount) || 1;

    // Base de datos
    const dbConnections = this.extractMetricValue(dbActive) || 0;
    const dbMaxConnections = this.extractMetricValue(dbMax) || 10;

    // Uptime
    const uptimeSeconds = this.extractMetricValue(uptime) || 0;

    // Disco
    const diskFreeBytes = this.extractMetricValue(diskFree) || 0;
    const diskTotalBytes = this.extractMetricValue(diskTotal) || 1;
    const diskUsedBytes = diskTotalBytes - diskFreeBytes;
    const diskUsagePercentage = Math.min(100, Math.max(0, (diskUsedBytes / diskTotalBytes) * 100));

    return {
      memory: {
        used: usedMemoryMB,
        max: maxMemoryMB,
        percentage: Math.round(memoryPercentage * 100) / 100
      },
      cpu: {
        usage: cpuUsageValue,
        load: Math.round(cpuUsageValue * 100 * 100) / 100,
        cores: cpuCores
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
      },
      disk: {
        used: Math.round(diskUsedBytes / (1024 * 1024 * 1024)), // GB
        total: Math.round(diskTotalBytes / (1024 * 1024 * 1024)), // GB
        percentage: Math.round(diskUsagePercentage * 100) / 100
      }
    };
  }

  /**
   * Valida y sanitiza las métricas de CPU
   */
  private validateCpuUsage(systemCpu: any, processCpu: any): number {
    let cpuUsage = 0;

    // Priorizar system.cpu.usage (más preciso)
    if (systemCpu?.measurements?.[0]?.value !== undefined) {
      cpuUsage = systemCpu.measurements[0].value;
    } else if (processCpu?.measurements?.[0]?.value !== undefined) {
      cpuUsage = processCpu.measurements[0].value;
    }

    // Validar que el valor sea razonable
    if (cpuUsage < 0 || cpuUsage > this.CPU_THRESHOLD) {
      console.warn(`⚠️ Valor de CPU sospechoso: ${cpuUsage}, usando valor por defecto`);
      cpuUsage = 0.1; // 10% por defecto
    }

    // Si el valor es muy alto, verificar si es real
    if (cpuUsage > 0.8) {
      console.warn(`⚠️ Uso de CPU alto detectado: ${(cpuUsage * 100).toFixed(1)}%`);
    }

    return cpuUsage;
  }

  /**
   * Extrae el valor de una métrica de Actuator
   */
  private extractMetricValue(metric: any): number | null {
    if (!metric || !metric.measurements || !Array.isArray(metric.measurements)) {
      return null;
    }

    const measurement = metric.measurements[0];
    if (!measurement || typeof measurement.value !== 'number') {
      return null;
    }

    return measurement.value;
  }

  /**
   * Valida y sanitiza todas las métricas
   */
  private validateAndSanitizeMetrics(metrics: SystemMetrics): SystemMetrics {
    // Validar memoria
    if (metrics.memory.percentage > this.MEMORY_THRESHOLD * 100) {
      console.warn(`⚠️ Uso de memoria alto: ${metrics.memory.percentage.toFixed(1)}%`);
    }

    // Validar disco
    if (metrics.disk.percentage > this.DISK_THRESHOLD * 100) {
      console.warn(`⚠️ Uso de disco alto: ${metrics.disk.percentage.toFixed(1)}%`);
    }

    // Validar conexiones de base de datos
    if (metrics.database.connections > metrics.database.maxConnections) {
      console.warn(`⚠️ Conexiones de BD exceden el máximo: ${metrics.database.connections}/${metrics.database.maxConnections}`);
      metrics.database.connections = Math.min(metrics.database.connections, metrics.database.maxConnections);
    }

    return metrics;
  }

  /**
   * Obtiene métricas básicas en caso de error
   */
  private getFallbackMetrics(): SystemMetrics {
    return {
      memory: { used: 0, max: 0, percentage: 0 },
      cpu: { usage: 0, load: 0, cores: 1 },
      database: { connections: 0, maxConnections: 10, status: 'UNKNOWN' },
      redis: { status: 'UNKNOWN', operations: 0 },
      app: { status: 'UNKNOWN', uptime: 0, schedule: 'Desconocido', version: '1.0.0' },
      disk: { used: 0, total: 0, percentage: 0 }
    };
  }

  /**
   * Obtiene información del horario actual
   */
  private getScheduleInfo(): string {
    const now = new Date();
    const hour = now.getHours();
    
    if (hour >= 6 && hour < 12) return 'Mañana (6:00-12:00)';
    if (hour >= 12 && hour < 18) return 'Tarde (12:00-18:00)';
    if (hour >= 18 && hour < 22) return 'Noche (18:00-22:00)';
    return 'Madrugada (22:00-6:00)';
  }

  /**
   * Fuerza una actualización de métricas
   */
  public refreshMetrics(): void {
    this.loadMetrics().subscribe();
  }

  /**
   * Obtiene las métricas actuales
   */
  public getCurrentMetrics(): SystemMetrics | null {
    return this.metricsSubject.value;
  }

  /**
   * Verifica si el sistema está en estado crítico
   */
  public isSystemCritical(): boolean {
    const metrics = this.getCurrentMetrics();
    if (!metrics) return false;

    return (
      metrics.memory.percentage > 90 ||
      metrics.cpu.usage > 0.9 ||
      metrics.disk.percentage > 90
    );
  }

  /**
   * Obtiene recomendaciones de optimización locales
   */
  public getLocalOptimizationRecommendations(): string[] {
    const metrics = this.getCurrentMetrics();
    if (!metrics) return [];

    const recommendations: string[] = [];

    if (metrics.memory.percentage > 80) {
      recommendations.push('Memoria alta: Considerar limpieza de caché o reinicio');
    }

    if (metrics.cpu.usage > 0.7) {
      recommendations.push('CPU alta: Verificar procesos en segundo plano');
    }

    if (metrics.disk.percentage > 80) {
      recommendations.push('Disco alto: Limpiar archivos temporales');
    }

    if (metrics.database.connections > metrics.database.maxConnections * 0.8) {
      recommendations.push('Conexiones BD altas: Verificar conexiones abiertas');
    }

    return recommendations;
  }

  /**
   * Obtiene métricas completas del sistema desde el backend
   */
  public getCompleteMetrics(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/api/system/metrics`);
  }

  /**
   * Obtiene recomendaciones de optimización desde el backend
   */
  public getOptimizationRecommendations(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/api/system/optimization/recommendations`);
  }
}
