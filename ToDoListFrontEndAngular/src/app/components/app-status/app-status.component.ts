import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface AppStatus {
  active: boolean;
  schedule: string;
  nextStart: string;
  timestamp: number;
}

@Component({
  selector: 'app-app-status',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="app-status" *ngIf="showStatus">
      <div class="status-indicator" [class.active]="status?.active" [class.inactive]="!status?.active">
        <span class="status-dot"></span>
        <span class="status-text">
          {{ status?.active ? '🟢 Activa' : '🔴 Inactiva' }}
        </span>
      </div>
      
      <div class="schedule-info" *ngIf="status?.schedule !== 'Siempre activa'">
        <div class="schedule-time">
          <strong>Horario:</strong> {{ status?.schedule }}
        </div>
        <div class="next-start" *ngIf="!status?.active">
          <strong>Próxima activación:</strong> {{ status?.nextStart }}
        </div>
      </div>
      
      <div class="last-update">
        Última actualización: {{ getLastUpdate() }}
      </div>
    </div>
  `,
  styles: [`
    .app-status {
      background: rgba(0, 0, 0, 0.05);
      border-radius: 8px;
      padding: 12px;
      margin: 10px 0;
      font-size: 14px;
    }
    
    .status-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
    }
    
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #ccc;
    }
    
    .status-dot.active {
      background: #4caf50;
    }
    
    .status-dot.inactive {
      background: #f44336;
    }
    
    .schedule-info {
      margin: 8px 0;
      font-size: 13px;
    }
    
    .last-update {
      font-size: 12px;
      color: #666;
      font-style: italic;
    }
  `]
})
export class AppStatusComponent implements OnInit, OnDestroy {
  status: AppStatus | null = null;
  showStatus = false;
  private interval: any;
  
  constructor(private http: HttpClient) {}
  
  ngOnInit() {
    // Solo mostrar en producción
    this.showStatus = !environment.production;
    if (this.showStatus) {
      this.loadStatus();
      this.interval = setInterval(() => this.loadStatus(), 30000); // Actualizar cada 30 segundos
    }
  }
  
  ngOnDestroy() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  }
  
  loadStatus() {
    this.http.get<AppStatus>(`${environment.apiUrl}/api/app-status/status`)
      .subscribe({
        next: (data) => this.status = data,
        error: (error) => console.error('Error cargando estado:', error)
      });
  }
  
  getLastUpdate(): string {
    if (!this.status?.timestamp) return 'N/A';
    return new Date(this.status.timestamp).toLocaleTimeString('es-ES');
  }
}
