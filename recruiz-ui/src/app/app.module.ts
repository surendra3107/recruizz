import { BrowserModule } from '@angular/platform-browser';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// Import your AvatarModule
import { AvatarModule } from 'ngx-avatar';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './material-module/material.module';
import { AuthComponent } from './auth/auth.component';
import { RecaptchaModule } from 'ng-recaptcha';

import { NgHttpLoaderModule } from 'ng-http-loader';

//cloud word
import { TagCloudModule } from 'angular-tag-cloud-module';

import { AuthTenantComponent } from './auth-tenant/auth-tenant.component';

import { ErrorDialogComponent } from './error-dialog/errordialog.component';
import { ErrorDialogService } from './error-dialog/errordialog.service';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpConfigInterceptor } from './interceptor/httpconfig.interceptor';

import { DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { CalendarModule } from 'angular-calendar';


@NgModule({
    declarations: [
        AppComponent,
        AuthComponent,
        AuthTenantComponent,
        ErrorDialogComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MaterialModule,
        FormsModule,
        ReactiveFormsModule,
        RecaptchaModule,
        HttpClientModule,
        NgHttpLoaderModule.forRoot(),
        AvatarModule,
        TagCloudModule,
        CalendarModule.forRoot({ provide: DateAdapter, useFactory: adapterFactory })
    ],
    providers: [
        ErrorDialogService,
        { provide: HTTP_INTERCEPTORS, useClass: HttpConfigInterceptor, multi: true }
        // provider used to create fake backend
    ],
    bootstrap: [AppComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule { }
