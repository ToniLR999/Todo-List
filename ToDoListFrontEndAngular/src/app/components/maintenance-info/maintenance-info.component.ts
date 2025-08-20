import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduleService, ScheduleStatus } from '../../services/schedule.service';

@Component({
  selector: 'app-maintenance-info',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './maintenance-info.component.html',
  styleUrls: ['./maintenance-info.component.css']
})
export class MaintenanceInfoComponent implements OnInit {
  scheduleStatus: ScheduleStatus | null = null;
  isLoading = false;
  error = false;

  constructor(public scheduleService: ScheduleService) {}

  ngOnInit() {
    this.loadScheduleStatus();
  }

  loadScheduleStatus() {
    this.isLoading = true;
    this.error = false;
    
    try {
      this.scheduleStatus = this.scheduleService.getCurrentScheduleStatus();
      this.isLoading = false;
    } catch (err) {
      console.error('Error loading schedule status:', err);
      this.error = true;
      this.isLoading = false;
    }
  }

  refreshInfo() {
    this.loadScheduleStatus();
  }

  getStatusIcon(): string {
    if (!this.scheduleStatus) return '❓';
    
    switch (this.scheduleStatus.status) {
      case 'ACTIVE':
        return '✅';
      case 'INACTIVE':
        return '⏰';
      case 'WEEKEND':
        return '🏖️';
      default:
        return '❓';
    }
  }

  getStatusColor(): string {
    if (!this.scheduleStatus) return 'text-gray-500';
    
    switch (this.scheduleStatus.status) {
      case 'ACTIVE':
        return 'text-green-600';
      case 'INACTIVE':
        return 'text-orange-600';
      case 'WEEKEND':
        return 'text-blue-600';
      default:
        return 'text-gray-500';
    }
  }
}