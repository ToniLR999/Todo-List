import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {
  username: string = '';
  dropdownOpen = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.username = this.authService.getUsername() || 'Usuario';
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    setTimeout(() => this.dropdownOpen = false, 150); // Permite click en opciones
  }

  goToNotifications() {
    this.router.navigate(['/notifications']);
    this.dropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.dropdownOpen = false;
  }
}