import { Injectable } from '@angular/core';
import { Subject, Observable, timer } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DebounceService {
  private searchSubject = new Subject<string>();
  private filterSubject = new Subject<any>();

  constructor() {}

  /**
   * Crea un observable de búsqueda con debounce
   * @param delay Tiempo de espera en milisegundos (por defecto 300ms)
   * @returns Observable que emite después del delay
   */
  createSearchStream(delay: number = 300): Observable<string> {
    return this.searchSubject.pipe(
      debounceTime(delay),
      distinctUntilChanged()
    );
  }

  /**
   * Crea un observable de filtros con debounce
   * @param delay Tiempo de espera en milisegundos (por defecto 500ms)
   * @returns Observable que emite después del delay
   */
  createFilterStream(delay: number = 500): Observable<any> {
    return this.filterSubject.pipe(
      debounceTime(delay),
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr))
    );
  }

  /**
   * Emite un valor de búsqueda
   * @param value Valor a buscar
   */
  emitSearch(value: string): void {
    this.searchSubject.next(value);
  }

  /**
   * Emite un valor de filtro
   * @param value Valor del filtro
   */
  emitFilter(value: any): void {
    this.filterSubject.next(value);
  }

  /**
   * Crea un observable que emite después de un delay específico
   * @param delay Tiempo de espera en milisegundos
   * @returns Observable que emite después del delay
   */
  createDelayedStream<T>(delay: number): Observable<T> {
    return timer(delay).pipe(
      switchMap(() => new Subject<T>())
    );
  }

  /**
   * Limpia todos los observables
   */
  clear(): void {
    this.searchSubject.complete();
    this.filterSubject.complete();
  }
}
