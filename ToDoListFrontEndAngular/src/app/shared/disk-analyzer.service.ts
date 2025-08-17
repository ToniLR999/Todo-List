import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface DiskUsageItem {
  path: string;
  size: number;
  sizeFormatted: string;
  percentage: number;
  type: 'file' | 'directory' | 'log' | 'cache' | 'database' | 'backup' | 'temp';
  lastModified: string;
  description: string;
}

export interface DiskAnalysis {
  totalSpace: number;
  usedSpace: number;
  freeSpace: number;
  usagePercentage: number;
  topConsumers: DiskUsageItem[];
  recommendations: string[];
  criticalPaths: string[];
  lastAnalysis: string;
}

@Injectable({
  providedIn: 'root'
})
export class DiskAnalyzerService {
  private analysisSubject = new BehaviorSubject<DiskAnalysis | null>(null);
  private isAnalyzingSubject = new BehaviorSubject<boolean>(false);
  
  public analysis$ = this.analysisSubject.asObservable();
  public isAnalyzing$ = this.isAnalyzingSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Analiza el uso del disco y identifica los principales consumidores
   */
  public analyzeDiskUsage(): Observable<DiskAnalysis> {
    this.isAnalyzingSubject.next(true);

    return this.http.get(`${environment.apiUrl}/api/system/disk/analysis`).pipe(
      map(response => this.processBackendAnalysis(response)),
      tap(analysis => {
        this.analysisSubject.next(analysis);
        this.isAnalyzingSubject.next(false);
      }),
      catchError(error => {
        console.error('❌ Error analizando disco:', error);
        this.isAnalyzingSubject.next(false);
        
        // Retornar análisis básico en caso de error
        const fallbackAnalysis = this.getFallbackAnalysis();
        this.analysisSubject.next(fallbackAnalysis);
        return of(fallbackAnalysis);
      })
    );
  }

  /**
   * Procesa la respuesta del backend para el análisis de disco
   */
  private processBackendAnalysis(response: any): DiskAnalysis {
    try {
      if (response?.status === 'SUCCESS') {
        const diskInfo = response.diskInfo;
        const topConsumers = response.topConsumers || [];
        const criticalPaths = response.criticalPaths || [];
        const recommendations = response.recommendations || [];
        
        return {
          totalSpace: diskInfo?.total || 0,
          usedSpace: diskInfo?.used || 0,
          freeSpace: diskInfo?.free || 0,
          usagePercentage: diskInfo?.usagePercentage || 0,
          topConsumers: topConsumers.map((item: any) => ({
            path: item.path,
            size: item.size || 0,
            sizeFormatted: item.sizeFormatted || this.formatBytes(item.size || 0),
            percentage: item.percentage || 0,
            type: item.type || 'file',
            lastModified: item.lastModified || new Date().toISOString(),
            description: item.description || 'Archivo del sistema'
          })),
          recommendations,
          criticalPaths,
          lastAnalysis: response.lastAnalysis || new Date().toISOString()
        };
      } else {
        console.warn('⚠️ Respuesta del backend no exitosa:', response);
        return this.getFallbackAnalysis();
      }
    } catch (error) {
      console.error('❌ Error procesando respuesta del backend:', error);
      return this.getFallbackAnalysis();
    }
  }

  /**
   * Extrae información de disco del endpoint de health (método legacy)
   */
  private extractDiskInfoFromHealth(health: any): any {
    const diskSpace = health?.components?.diskSpace;
    
    if (!diskSpace) {
      console.warn('⚠️ No se encontró información de disco en health endpoint');
      return null;
    }

    return {
      total: diskSpace.details?.total || 0,
      free: diskSpace.details?.free || 0,
      threshold: diskSpace.details?.threshold || 0
    };
  }

  /**
   * Analiza el consumo de disco y genera recomendaciones
   */
  private analyzeDiskConsumption(diskInfo: any): DiskAnalysis {
    if (!diskInfo) {
      return this.getFallbackAnalysis();
    }

    const totalSpace = diskInfo.total;
    const freeSpace = diskInfo.free;
    const usedSpace = totalSpace - freeSpace;
    const usagePercentage = totalSpace > 0 ? (usedSpace / totalSpace) * 100 : 0;

    // Simular análisis de directorios (en producción esto vendría del backend)
    const topConsumers = this.simulateDirectoryAnalysis(usedSpace, totalSpace);
    
    // Generar recomendaciones basadas en el uso
    const recommendations = this.generateRecommendations(usagePercentage, topConsumers);
    
    // Identificar rutas críticas
    const criticalPaths = this.identifyCriticalPaths(topConsumers);

    return {
      totalSpace,
      usedSpace,
      freeSpace,
      usagePercentage: Math.round(usagePercentage * 100) / 100,
      topConsumers,
      recommendations,
      criticalPaths,
      lastAnalysis: new Date().toISOString()
    };
  }

  /**
   * Simula análisis de directorios (en producción esto sería real)
   */
  private simulateDirectoryAnalysis(usedSpace: number, totalSpace: number): DiskUsageItem[] {
    const now = new Date();
    
    return [
      {
        path: '/var/log',
        size: Math.round(usedSpace * 0.25), // 25% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.25)),
        percentage: 25,
        type: 'log',
        lastModified: now.toISOString(),
        description: 'Archivos de log del sistema y aplicaciones'
      },
      {
        path: '/tmp',
        size: Math.round(usedSpace * 0.20), // 20% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.20)),
        percentage: 20,
        type: 'temp',
        lastModified: now.toISOString(),
        description: 'Archivos temporales del sistema'
      },
      {
        path: '/var/cache',
        size: Math.round(usedSpace * 0.15), // 15% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.15)),
        percentage: 15,
        type: 'cache',
        lastModified: now.toISOString(),
        description: 'Caché del sistema y aplicaciones'
      },
      {
        path: '/home/user',
        size: Math.round(usedSpace * 0.15), // 15% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.15)),
        percentage: 15,
        type: 'directory',
        lastModified: now.toISOString(),
        description: 'Archivos del usuario'
      },
      {
        path: '/var/lib/mysql',
        size: Math.round(usedSpace * 0.10), // 10% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.10)),
        percentage: 10,
        type: 'database',
        lastModified: now.toISOString(),
        description: 'Base de datos MySQL'
      },
      {
        path: '/var/backups',
        size: Math.round(usedSpace * 0.05), // 5% del espacio usado
        sizeFormatted: this.formatBytes(Math.round(usedSpace * 0.05)),
        percentage: 5,
        type: 'backup',
        lastModified: now.toISOString(),
        description: 'Archivos de respaldo'
      }
    ];
  }

  /**
   * Genera recomendaciones basadas en el uso del disco
   */
  private generateRecommendations(usagePercentage: number, topConsumers: DiskUsageItem[]): string[] {
    const recommendations: string[] = [];

    if (usagePercentage > 90) {
      recommendations.push('🚨 CRÍTICO: El disco está casi lleno. Liberar espacio inmediatamente.');
      recommendations.push('🗑️ Eliminar archivos temporales y caché no esenciales');
      recommendations.push('📊 Revisar logs antiguos y rotar archivos de log');
      recommendations.push('💾 Considerar limpieza de base de datos y respaldos antiguos');
    } else if (usagePercentage > 80) {
      recommendations.push('⚠️ ALTO: El disco está muy ocupado. Planificar limpieza.');
      recommendations.push('🧹 Ejecutar limpieza automática de archivos temporales');
      recommendations.push('📝 Configurar rotación automática de logs');
      recommendations.push('🗂️ Revisar archivos grandes y duplicados');
    } else if (usagePercentage > 70) {
      recommendations.push('📈 MODERADO: Monitorear el crecimiento del uso de disco');
      recommendations.push('📋 Implementar políticas de retención de archivos');
      recommendations.push('🔍 Configurar alertas cuando se alcance el 80%');
    } else {
      recommendations.push('✅ NORMAL: El uso del disco está en niveles saludables');
    }

    // Recomendaciones específicas basadas en los principales consumidores
    const logItems = topConsumers.filter(item => item.type === 'log');
    if (logItems.length > 0) {
      recommendations.push('📋 Configurar rotación de logs: mantener solo últimos 7-30 días');
    }

    const tempItems = topConsumers.filter(item => item.type === 'temp');
    if (tempItems.length > 0) {
      recommendations.push('🗑️ Limpiar archivos temporales: /tmp, /var/tmp, caché del navegador');
    }

    const cacheItems = topConsumers.filter(item => item.type === 'cache');
    if (cacheItems.length > 0) {
      recommendations.push('🧹 Limpiar caché: apt, yum, npm, maven, gradle');
    }

    return recommendations;
  }

  /**
   * Identifica rutas críticas que requieren atención inmediata
   */
  private identifyCriticalPaths(topConsumers: DiskUsageItem[]): string[] {
    const criticalPaths: string[] = [];

    // Agregar rutas críticas basadas en el tipo y tamaño
    topConsumers.forEach(item => {
      if (item.percentage > 20) {
        criticalPaths.push(`${item.path} (${item.sizeFormatted})`);
      }
      
      if (item.type === 'log' && item.percentage > 15) {
        criticalPaths.push(`${item.path} - Logs muy grandes`);
      }
      
      if (item.type === 'temp' && item.percentage > 10) {
        criticalPaths.push(`${item.path} - Muchos archivos temporales`);
      }
    });

    return criticalPaths;
  }

  /**
   * Formatea bytes en unidades legibles
   */
  private formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';
    
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Obtiene análisis básico en caso de error
   */
  private getFallbackAnalysis(): DiskAnalysis {
    return {
      totalSpace: 0,
      usedSpace: 0,
      freeSpace: 0,
      usagePercentage: 0,
      topConsumers: [],
      recommendations: ['❌ No se pudo analizar el disco. Verificar conectividad.'],
      criticalPaths: [],
      lastAnalysis: new Date().toISOString()
    };
  }

  /**
   * Fuerza un nuevo análisis del disco
   */
  public refreshAnalysis(): void {
    this.analyzeDiskUsage().subscribe();
  }

  /**
   * Ejecuta limpieza del disco usando el backend
   */
  public performCleanup(): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/system/cleanup`, {});
  }

  /**
   * Obtiene el análisis actual
   */
  public getCurrentAnalysis(): DiskAnalysis | null {
    return this.analysisSubject.value;
  }

  /**
   * Verifica si el disco está en estado crítico
   */
  public isDiskCritical(): boolean {
    const analysis = this.getCurrentAnalysis();
    return analysis ? analysis.usagePercentage > 90 : false;
  }

  /**
   * Obtiene el tamaño total del disco en GB
   */
  public getDiskSizeGB(): number {
    const analysis = this.getCurrentAnalysis();
    return analysis ? Math.round(analysis.totalSpace / (1024 * 1024 * 1024)) : 0;
  }

  /**
   * Obtiene el espacio libre en GB
   */
  public getFreeSpaceGB(): number {
    const analysis = this.getCurrentAnalysis();
    return analysis ? Math.round(analysis.freeSpace / (1024 * 1024 * 1024)) : 0;
  }
}
