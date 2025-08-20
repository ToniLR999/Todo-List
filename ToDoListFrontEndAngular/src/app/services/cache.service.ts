import { Injectable } from '@angular/core';

interface CacheItem<T> {
  data: T;
  timestamp: number;
  ttl: number;
}

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private cache = new Map<string, CacheItem<any>>();
  private readonly DEFAULT_TTL = 5 * 60 * 1000; // 5 minutos por defecto

  constructor() {
    // Limpiar cache expirado cada minuto
    setInterval(() => this.cleanupExpiredCache(), 60000);
  }

  /**
   * Almacena un elemento en el cache con TTL configurable
   */
  set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    });
  }

  /**
   * Obtiene un elemento del cache si no ha expirado
   */
  get<T>(key: string): T | null {
    const item = this.cache.get(key);
    
    if (!item) {
      return null;
    }

    // Verificar si ha expirado
    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(key);
      return null;
    }

    return item.data;
  }

  /**
   * Verifica si existe un elemento en el cache y no ha expirado
   */
  has(key: string): boolean {
    const item = this.cache.get(key);
    
    if (!item) {
      return false;
    }

    // Verificar si ha expirado
    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(key);
      return false;
    }

    return true;
  }

  /**
   * Elimina un elemento específico del cache
   */
  delete(key: string): boolean {
    return this.cache.delete(key);
  }

  /**
   * Limpia todo el cache
   */
  clear(): void {
    this.cache.clear();
  }

  /**
   * Limpia solo el cache expirado
   */
  private cleanupExpiredCache(): void {
    const now = Date.now();
    
    for (const [key, item] of this.cache.entries()) {
      if (now - item.timestamp > item.ttl) {
        this.cache.delete(key);
      }
    }
  }

  /**
   * Obtiene estadísticas del cache
   */
  getStats(): { size: number; keys: string[] } {
    return {
      size: this.cache.size,
      keys: Array.from(this.cache.keys())
    };
  }

  /**
   * Genera una clave de cache basada en parámetros
   */
  generateKey(prefix: string, params: any): string {
    const paramString = JSON.stringify(params);
    return `${prefix}:${this.hashCode(paramString)}`;
  }

  /**
   * Función hash simple para generar claves únicas
   */
  private hashCode(str: string): string {
    let hash = 0;
    if (str.length === 0) return hash.toString();
    
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convertir a entero de 32 bits
    }
    
    return Math.abs(hash).toString();
  }
}