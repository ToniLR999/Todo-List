import { Injectable } from '@angular/core';

export interface CacheItem<T> {
  data: T;
  timestamp: number;
  ttl: number; // Time to live in milliseconds
}

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private readonly CACHE_PREFIX = 'todolist_';
  private readonly DEFAULT_TTL = 5 * 60 * 1000; // 5 minutos

  // Guardar en caché
  set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl
    };
    
    try {
      localStorage.setItem(this.CACHE_PREFIX + key, JSON.stringify(cacheItem));
      console.log('✅ Caché guardado:', key, data);
    } catch (error) {
      console.warn('Error al guardar en caché:', error);
      this.cleanup(); // Limpiar caché si está lleno
    }
  }

  // Obtener del caché
  get<T>(key: string): T | null {
    try {
      const item = localStorage.getItem(this.CACHE_PREFIX + key);
      if (!item) return null;

      const cacheItem: CacheItem<T> = JSON.parse(item);
      
      // Verificar si ha expirado
      if (Date.now() - cacheItem.timestamp > cacheItem.ttl) {
        this.remove(key);
        return null;
      }

      return cacheItem.data;
    } catch (error) {
      console.warn('Error al obtener del caché:', error);
      return null;
    }
  }

  // Eliminar del caché
  remove(key: string): void {
    localStorage.removeItem(this.CACHE_PREFIX + key);
  }

  // Limpiar todo el caché
  clear(): void {
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.startsWith(this.CACHE_PREFIX)) {
        localStorage.removeItem(key);
      }
    });
  }

  // Limpiar caché expirado
  cleanup(): void {
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.startsWith(this.CACHE_PREFIX)) {
        try {
          const item = localStorage.getItem(key);
          if (item) {
            const cacheItem: CacheItem<any> = JSON.parse(item);
            if (Date.now() - cacheItem.timestamp > cacheItem.ttl) {
              localStorage.removeItem(key);
            }
          }
        } catch (error) {
          localStorage.removeItem(key);
        }
      }
    });
  }

  // Verificar si existe en caché
  has(key: string): boolean {
    return this.get(key) !== null;
  }

  // Obtener tamaño del caché
  getSize(): number {
    let size = 0;
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.startsWith(this.CACHE_PREFIX)) {
        size += localStorage.getItem(key)?.length || 0;
      }
    });
    return size;
  }
}