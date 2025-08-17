import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, interval, Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MemoryManagerService {
  private memoryUsage$ = new BehaviorSubject<number>(0);
  private cpuUsage$ = new BehaviorSubject<number>(0);
  private destroy$ = new Subject<void>();
  private memoryThreshold = 80; // 80% de uso de memoria
  private cpuThreshold = 70; // 70% de uso de CPU

  private optimizationNeededSubject$ = new BehaviorSubject<boolean>(false);


  constructor(private ngZone: NgZone) {
    this.startMemoryMonitoring();
  }

  /**
   * Inicia el monitoreo de memoria y CPU
   */
  private startMemoryMonitoring(): void {
    // Monitorear cada 30 segundos para no impactar el rendimiento
    interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.ngZone.runOutsideAngular(() => {
          this.checkMemoryUsage();
          this.checkCpuUsage();
        });
      });
  }

  /**
   * Verifica el uso de memoria del navegador
   */
  private checkMemoryUsage(): void {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      const usedMemoryMB = memory.usedJSHeapSize / 1024 / 1024;
      const totalMemoryMB = memory.totalJSHeapSize / 1024 / 1024;
      const memoryUsagePercent = (usedMemoryMB / totalMemoryMB) * 100;

      this.memoryUsage$.next(memoryUsagePercent);

      // Si el uso de memoria es alto, sugerir limpieza
      if (memoryUsagePercent > this.memoryThreshold) {
        this.suggestMemoryCleanup();
      }
    }
  }

  /**
   * Verifica el uso de CPU (aproximado)
   */
  private checkCpuUsage(): void {
    // Simulación del uso de CPU basada en el tiempo de respuesta
    const startTime = performance.now();
    
    // Tarea ligera para medir rendimiento
    setTimeout(() => {
      const endTime = performance.now();
      const responseTime = endTime - startTime;
      
      // Calcular uso de CPU aproximado basado en tiempo de respuesta
      const cpuUsage = Math.min(100, Math.max(0, (responseTime - 1) * 10));
      this.cpuUsage$.next(cpuUsage);

      if (cpuUsage > this.cpuThreshold) {
        this.suggestCpuOptimization();
      }
    }, 1);
  }

  /**
   * Sugiere limpieza de memoria
   */
  private suggestMemoryCleanup(): void {
    console.warn('Uso de memoria alto detectado. Considerando limpieza...');
    
    // Forzar garbage collection si está disponible
    if ('gc' in window) {
      (window as any).gc();
    }

    // Limpiar caché del navegador
    if ('caches' in window) {
      caches.keys().then(names => {
        names.forEach(name => {
          if (name !== 'app-cache-v1') {
            caches.delete(name);
          }
        });
      });
    }
  }

  /**
   * Sugiere optimizaciones de CPU
   */
  private suggestCpuOptimization(): void {
    console.warn('Uso de CPU alto detectado. Considerando optimizaciones...');
    
    // Reducir la frecuencia de actualizaciones
    this.reduceUpdateFrequency();
  }

  /**
   * Reduce la frecuencia de actualizaciones para ahorrar CPU
   */
  private reduceUpdateFrequency(): void {
    // Implementar throttling de eventos
    this.ngZone.run(() => {
      // Notificar a los componentes para que reduzcan actualizaciones
      this.optimizationNeededSubject$.next(true);
    });
  }

  /**
   * Limpia recursos y libera memoria
   */
  public cleanup(): void {
    // Limpiar observables
    this.destroy$.next();
    this.destroy$.complete();

    // Limpiar caché local
    if (localStorage.getItem('app-cache')) {
      localStorage.removeItem('app-cache');
    }

    // Forzar garbage collection
    if ('gc' in window) {
      (window as any).gc();
    }

    console.log('Limpieza de memoria completada');
  }

  /**
   * Obtiene el uso actual de memoria
   */
  public getMemoryUsage(): Observable<number> {
    return this.memoryUsage$.asObservable();
  }

  /**
   * Obtiene el uso actual de CPU
   */
  public getCpuUsage(): Observable<number> {
    return this.cpuUsage$.asObservable();
  }

  /**
   * Verifica si se necesita optimización
   */
  public get optimizationNeeded$(): Observable<boolean> {
    return new BehaviorSubject<boolean>(false).asObservable();
  }

  /**
   * Optimiza el rendimiento de la aplicación
   */
  public optimizePerformance(): void {
    // Reducir la frecuencia de actualizaciones de UI
    this.ngZone.runOutsideAngular(() => {
      // Implementar throttling de eventos del DOM
      this.throttleDOMEvents();
    });
  }

  /**
   * Implementa throttling de eventos del DOM
   */
  private throttleDOMEvents(): void {
    // Throttling para eventos de scroll y resize
    let ticking = false;
    
    const updateLayout = () => {
      ticking = false;
      // Actualizar layout solo cuando sea necesario
    };

    const requestTick = () => {
      if (!ticking) {
        requestAnimationFrame(updateLayout);
        ticking = true;
      }
    };

    // Aplicar throttling a eventos comunes
    window.addEventListener('scroll', requestTick, { passive: true });
    window.addEventListener('resize', requestTick, { passive: true });
  }

  /**
   * Configura el nivel de optimización
   */
  public setOptimizationLevel(level: 'low' | 'medium' | 'high'): void {
    switch (level) {
      case 'low':
        this.memoryThreshold = 90;
        this.cpuThreshold = 80;
        break;
      case 'medium':
        this.memoryThreshold = 80;
        this.cpuThreshold = 70;
        break;
      case 'high':
        this.memoryThreshold = 70;
        this.cpuThreshold = 60;
        break;
    }
  }

  /**
   * Obtiene estadísticas de rendimiento
   */
  public getPerformanceStats(): any {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      return {
        usedJSHeapSize: memory.usedJSHeapSize / 1024 / 1024, // MB
        totalJSHeapSize: memory.totalJSHeapSize / 1024 / 1024, // MB
        jsHeapSizeLimit: memory.jsHeapSizeLimit / 1024 / 1024, // MB
        memoryUsage: this.memoryUsage$.value,
        cpuUsage: this.cpuUsage$.value
      };
    }
    return null;
  }
}
