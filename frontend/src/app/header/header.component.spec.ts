import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HeaderComponent } from './header.component';
import { AuthService } from '../auth/auth.service';
import { EnderecoService } from '../services/enderecoService/endereco.service';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';

// Mocking AuthService and EnderecoService
class AuthServiceMock {
  isAuthenticated() {
    return of(true);
  }
  getUserId() {
    return of('123');
  }
}

class EnderecoServiceMock {
  getEnderecos(userId: string) {
    return of([{ id: '1', rua: 'Rua A' }]);
  }
}

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let routerSpy: jest.Mocked<Router>;

  beforeEach(async () => {
    routerSpy = { navigateByUrl: jest.fn() } as any;

    await TestBed.configureTestingModule({
      imports: [HeaderComponent, CommonModule, MatBadgeModule, MatIconModule],  // Mover HeaderComponent para imports
      providers: [
        { provide: AuthService, useClass: AuthServiceMock },
        { provide: EnderecoService, useClass: EnderecoServiceMock },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call router.navigateByUrl when goToAccount is called', () => {
    component.goToAccount();
    expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/minha-conta');
  });

  it('should load enderecos when loadEnderecos is called', () => {
    component.loadEnderecos();
    expect(component.enderecos.length).toBeGreaterThan(0);
  });

  it('should increment cartItemCount when addToCart is called', () => {
    const initialCount = component.cartItemCount;
    component.addToCart();
    expect(component.cartItemCount).toBe(initialCount + 1);
  });
});
