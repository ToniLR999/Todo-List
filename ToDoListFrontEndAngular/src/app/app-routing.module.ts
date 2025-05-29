import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { TaskListComponent } from './components/task-list/task-list.component';
import { AuthGuard } from './guards/auth.guard';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { TaskDetailComponent } from './components/task-detail/task-detail.component';

const routes: Routes = [
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
    canActivate: [AuthGuard],
    data: { animation: 'tasks' }
  },
  { 
    path: 'profile', 
    component: UserProfileComponent,
    canActivate: [AuthGuard],
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
  { path: 'tasks/:id', component: TaskDetailComponent },
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
