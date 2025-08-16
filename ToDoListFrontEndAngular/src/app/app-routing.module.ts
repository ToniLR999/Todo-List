import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { TaskListComponent } from './components/task-list/task-list.component';
import { AuthGuard } from './guards/auth.guard';
import { MaintenanceGuard } from './guards/maintenance.guard';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { TaskDetailComponent } from './components/task-detail/task-detail.component';
import { NotificationPreferencesComponent } from './components/notification-preferences/notification-preferences.component';
import { TaskListListComponent } from './components/task-list-list/task-list-list.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { MaintenanceInfoComponent } from './components/maintenance-info/maintenance-info.component';

/**
 * Application routes configuration with authentication guards and animations.
 * Protected routes require authentication via AuthGuard.
 * Maintenance routes check if application is active via MaintenanceGuard.
 */
export const routes: Routes = [
  { 
    path: 'maintenance', 
    component: MaintenanceInfoComponent,
    data: { animation: 'maintenance' }
  },
  { 
    path: 'login', 
    component: LoginComponent,
    data: { animation: 'login' }
  },
  { 
    path: 'register', 
    component: RegisterComponent,
    data: { animation: 'register' }
  },
  { 
    path: 'tasks', 
    component: TaskListComponent,
    canActivate: [MaintenanceGuard, AuthGuard],
    data: { animation: 'tasks' }
  },
  { 
    path: 'tasks/list/:id',
    component: TaskListComponent,
    canActivate: [MaintenanceGuard, AuthGuard]
  },
  { 
    path: 'tasks/:id',
    component: TaskDetailComponent,
    canActivate: [MaintenanceGuard, AuthGuard]
  },
  { 
    path: 'profile', 
    component: UserProfileComponent,
    canActivate: [MaintenanceGuard, AuthGuard],
    data: { animation: 'profile' }
  },
  { 
    path: 'forgot-password', 
    component: ForgotPasswordComponent,
    data: { animation: 'forgot-password' }
  },
  { 
    path: 'reset-password', 
    component: ResetPasswordComponent,
    data: { animation: 'reset-password' }
  },
  { path: 'notifications', component: NotificationPreferencesComponent },
  { path: 'perfil', component: UserProfileComponent },
  {
    path: 'lists',
    children: [
      { path: 'manage', component: TaskListListComponent }
    ]
  },
  { 
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [MaintenanceGuard, AuthGuard]
  },
  { 
    path: '', 
    redirectTo: '/tasks', 
    pathMatch: 'full' 
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      scrollPositionRestoration: 'enabled',
      anchorScrolling: 'enabled',
      scrollOffset: [0, 64]
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
