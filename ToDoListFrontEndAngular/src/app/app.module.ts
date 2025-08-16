import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { LoginComponent } from './components/login/login.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { NavComponent } from './components/nav/nav.component';
import { NotificationPreferencesComponent } from './components/notification-preferences/notification-preferences.component';
import { TaskListListComponent } from './components/task-list-list/task-list-list.component';
import { authInterceptor } from './interceptors/auth.interceptor';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';

@NgModule({
  declarations: [
    
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    ToastrModule.forRoot(),
    LoginComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    NotificationPreferencesComponent,
    NavComponent,
    TaskListListComponent,
    AdminDashboardComponent,
    CommonModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useValue: authInterceptor, multi: true }
  ],
  bootstrap: []
})
export class AppModule { }
