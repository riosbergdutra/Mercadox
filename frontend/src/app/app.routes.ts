import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { authGuard } from './auth/auth.guard';
import { HomepageComponent } from './homepage/homepage.component';
import { MinhaContaComponent } from './minha-conta/minha-conta.component';

export const routes: Routes = [
    {path:"", component: HomepageComponent},
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    {path:"minha-conta", component: MinhaContaComponent},  
    { path: '**', component: NotFoundComponent}

];
