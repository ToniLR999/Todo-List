/**
 * Cache service for managing local storage caching with TTL support.
 * Provides methods for storing, retrieving, and managing cached data
 * with automatic expiration and cleanup functionality.
 */
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

  /**
   * Stores data in cache with optional TTL.
   * @param key Cache key
   * @param data Data to cache
   * @param ttl Time to live in milliseconds (default: 5 minutes)
   */
  set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl
    };
    
    try {
      localStorage.setItem(this.CACHE_PREFIX + key, JSON.stringify(cacheItem));
      // console.log('✅ Caché guardado:', key, data);
    } catch (error) {
      console.warn('Error al guardar en caché:', error);
      this.cleanup(); // Limpiar caché si está lleno
    }
  }

  /**
   * Retrieves data from cache if not expired.
   * @param key Cache key
   * @returns Cached data or null if not found/expired
   */
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

  /**
   * Removes a specific item from cache.
   * @param key Cache key to remove
   */
  remove(key: string): void {
    localStorage.removeItem(this.CACHE_PREFIX + key);
  }

  /**
   * Clears all cached data with the cache prefix.
   */
  clear(): void {
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.startsWith(this.CACHE_PREFIX)) {
        localStorage.removeItem(key);
      }
    });
  }

  /**
   * Removes expired cache items to free up storage space.
   */
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

  /**
   * Checks if a key exists in cache and is not expired.
   * @param key Cache key to check
   * @returns true if key exists and is valid, false otherwise
   */
  has(key: string): boolean {
    return this.get(key) !== null;
  }

  /**
   * Gets the total size of cached data in bytes.
   * @returns Total size of cached data
   */
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