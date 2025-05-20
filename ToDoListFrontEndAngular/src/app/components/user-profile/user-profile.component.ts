import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth-service.service';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Validators } from '@angular/forms';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userProfile: any = {
    username: '',
    email: ''
  };
  isEditing: boolean = false;
  errorMessage: string = '';
  profileForm: FormGroup;
  success: string = '';
  error: string = '';

  constructor(
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.authService.getCurrentUser().subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.profileForm.patchValue({
          username: profile.username,
          email: profile.email
        });
      },
      error: (error) => {
        console.error('Error al cargar perfil:', error);
        this.error = 'Error al cargar el perfil';
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      const formData = {
        username: this.profileForm.get('username')?.value,
        email: this.profileForm.get('email')?.value
      };
      
      this.authService.updateProfile(formData).subscribe({
        next: (updatedProfile) => {
          this.userProfile = updatedProfile;
          this.isEditing = false;
          this.errorMessage = '';
          this.success = 'Perfil actualizado correctamente';
        },
        error: (error) => {
          console.error('Error al actualizar perfil:', error);
          this.errorMessage = 'Error al actualizar el perfil';
          this.success = '';
        }
      });
    }
  }
}
