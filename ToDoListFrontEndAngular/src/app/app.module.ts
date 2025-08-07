import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { LoginComponent } from './components/login/login.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { NavComponent } from './components/nav/nav.component';
import { NotificationPreferencesComponent } from './components/notification-preferences/notification-preferences.component';
import { authInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    ToastrModule.forRoot(),
    AppComponent,
    LoginComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    NotificationPreferencesComponent,
    NavComponent // <-- añádelo aquí
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useValue: authInterceptor, multi: true }
  ],
  bootstrap: []
})
export class AppModule { }
