import { Injectable, OnDestroy } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionManagerService implements OnDestroy {
  private destroy$ = new Subject<void>();

  /**
   * Método seguro para subscribirse que se auto-desuscribe al destruir el componente
   * @param observable Observable al que subscribirse
   * @param next Función next del observer
   * @param error Función error del observer (opcional)
   * @param complete Función complete del observer (opcional)
   */
  subscribe<T>(
    observable: any,
    next?: (value: T) => void,
    error?: (error: any) => void,
    complete?: () => void
  ): Subscription {
    return observable.pipe(takeUntil(this.destroy$)).subscribe({ next, error, complete });
  }

  /**
   * Método para agregar múltiples subscripciones
   * @param subscriptions Array de subscripciones
   */
  addSubscriptions(...subscriptions: Subscription[]): void {
    subscriptions.forEach(sub => {
      if (sub && !sub.closed) {
        // La subscripción se auto-desuscribirá al destruir el servicio
      }
    });
  }

  /**
   * Método para limpiar todas las subscripciones
   */
  clearSubscriptions(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnDestroy(): void {
    this.clearSubscriptions();
  }
}
