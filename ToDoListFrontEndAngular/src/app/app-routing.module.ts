import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { TaskListComponent } from './components/task-list/task-list.component';
import { AuthGuard } from './guards/auth.guard';
import { UserProfileComponent } from './components/user-profile/user-profile.component';

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
