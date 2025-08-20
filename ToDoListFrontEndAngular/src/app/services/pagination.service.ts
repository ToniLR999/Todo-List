import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface PaginationState {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export interface PaginatedResponse<T> {
  items: T[];
  pagination: PaginationState;
}

@Injectable({
  providedIn: 'root'
})
export class PaginationService {
  private paginationStateSubject = new BehaviorSubject<PaginationState>({
    currentPage: 1,
    pageSize: 20,
    totalItems: 0,
    totalPages: 0,
    hasNextPage: false,
    hasPreviousPage: false
  });

  public paginationState$ = this.paginationStateSubject.asObservable();

  constructor() {}

  /**
   * Obtiene el estado actual de paginación
   */
  getCurrentState(): PaginationState {
    return this.paginationStateSubject.value;
  }

  /**
   * Configura el tamaño de página
   */
  setPageSize(pageSize: number): void {
    const currentState = this.getCurrentState();
    const newState = {
      ...currentState,
      pageSize,
      currentPage: 1, // Reset a la primera página
      totalPages: Math.ceil(currentState.totalItems / pageSize)
    };
    
    this.updatePaginationState(newState);
  }

  /**
   * Va a una página específica
   */
  goToPage(page: number): void {
    const currentState = this.getCurrentState();
    
    if (page < 1 || page > currentState.totalPages) {
      return;
    }

    const newState = {
      ...currentState,
      currentPage: page
    };
    
    this.updatePaginationState(newState);
  }

  /**
   * Va a la página siguiente
   */
  nextPage(): void {
    const currentState = this.getCurrentState();
    if (currentState.hasNextPage) {
      this.goToPage(currentState.currentPage + 1);
    }
  }

  /**
   * Va a la página anterior
   */
  previousPage(): void {
    const currentState = this.getCurrentState();
    if (currentState.hasPreviousPage) {
      this.goToPage(currentState.currentPage - 1);
    }
  }

  /**
   * Va a la primera página
   */
  firstPage(): void {
    this.goToPage(1);
  }

  /**
   * Va a la última página
   */
  lastPage(): void {
    const currentState = this.getCurrentState();
    this.goToPage(currentState.totalPages);
  }

  /**
   * Actualiza el estado de paginación con nuevos datos
   */
  updatePaginationState(newState: Partial<PaginationState>): void {
    const currentState = this.getCurrentState();
    const updatedState = { ...currentState, ...newState };
    
    // Calcular propiedades derivadas
    updatedState.totalPages = Math.ceil(updatedState.totalItems / updatedState.pageSize);
    updatedState.hasNextPage = updatedState.currentPage < updatedState.totalPages;
    updatedState.hasPreviousPage = updatedState.currentPage > 1;
    
    this.paginationStateSubject.next(updatedState);
  }

  /**
   * Actualiza el total de elementos
   */
  updateTotalItems(totalItems: number): void {
    const currentState = this.getCurrentState();
    this.updatePaginationState({
      totalItems,
      totalPages: Math.ceil(totalItems / currentState.pageSize)
    });
  }

  /**
   * Resetea la paginación a su estado inicial
   */
  reset(): void {
    this.paginationStateSubject.next({
      currentPage: 1,
      pageSize: 20,
      totalItems: 0,
      totalPages: 0,
      hasNextPage: false,
      hasPreviousPage: false
    });
  }

  /**
   * Obtiene los parámetros de paginación para las llamadas HTTP
   */
  getPaginationParams(): { page: number; size: number } {
    const state = this.getCurrentState();
    return {
      page: state.currentPage - 1, // Backend usa 0-based indexing
      size: state.pageSize
    };
  }

  /**
   * Calcula el rango de elementos mostrados
   */
  getDisplayRange(): { start: number; end: number } {
    const state = this.getCurrentState();
    const start = (state.currentPage - 1) * state.pageSize + 1;
    const end = Math.min(state.currentPage * state.pageSize, state.totalItems);
    
    return { start, end };
  }

  /**
   * Verifica si la página actual es válida
   */
  isCurrentPageValid(): boolean {
    const state = this.getCurrentState();
    return state.currentPage >= 1 && state.currentPage <= state.totalPages;
  }

  /**
   * Obtiene un array de números de página para mostrar en la UI
   */
  getPageNumbers(maxVisible: number = 5): number[] {
    const state = this.getCurrentState();
    const pages: number[] = [];
    
    if (state.totalPages <= maxVisible) {
      // Mostrar todas las páginas si hay pocas
      for (let i = 1; i <= state.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Mostrar páginas alrededor de la actual
      const halfVisible = Math.floor(maxVisible / 2);
      let start = Math.max(1, state.currentPage - halfVisible);
      let end = Math.min(state.totalPages, start + maxVisible - 1);
      
      // Ajustar si estamos cerca del final
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }
}
