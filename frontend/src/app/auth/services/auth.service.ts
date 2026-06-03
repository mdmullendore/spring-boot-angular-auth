import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, tap, map, catchError } from 'rxjs';
import { environment } from '../../../environments/environment';

interface AuthResponse {
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private sessionKnown = signal<boolean | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap(() => this.sessionKnown.set(true)));
  }

  register(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, { email, password })
      .pipe(tap(() => this.sessionKnown.set(true)));
  }

  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => this.sessionKnown.set(false),
      error: () => this.sessionKnown.set(false),
      complete: () => this.router.navigate(['/auth/login'])
    });
  }

  checkSession(): Observable<boolean> {
    const cached = this.sessionKnown();
    if (cached !== null) {
      return of(cached);
    }

    return this.http.get<AuthResponse>(`${this.apiUrl}/me`).pipe(
      tap(() => this.sessionKnown.set(true)),
      map(() => true),
      catchError(() => {
        this.sessionKnown.set(false);
        return of(false);
      })
    );
  }
}
