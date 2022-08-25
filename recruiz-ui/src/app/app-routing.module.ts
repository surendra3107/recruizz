import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthComponent } from './auth/auth.component';
import { AuthTenantComponent } from './auth-tenant/auth-tenant.component';

const main_routes: Routes = [
  { 
    path: '', 
    redirectTo: 'web/login', 
    pathMatch: 'full'
  },
  { 
    path: 'web/login', 
    component: AuthComponent 
  },
  { 
    path: 'web/tenant', 
    component: AuthTenantComponent 
  },
  {
    path:'user',
    loadChildren: () => import('./user/user.module').then(m => m.UserModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(main_routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }