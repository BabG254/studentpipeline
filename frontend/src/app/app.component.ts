import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="container-fluid d-flex">
        <a class="navbar-brand" href="/">Student Data Pipeline</a>
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link" routerLink="/generate" routerLinkActive="active">Generate Data</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/process" routerLinkActive="active">Process Excel</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/upload" routerLinkActive="active">Upload CSV</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/report" routerLinkActive="active">Student Report</a>
          </li>
        </ul>
      </div>
    </nav>

    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .container-fluid {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 20px;
    }
    
    .navbar-nav {
      display: flex;
      list-style: none;
      margin: 0;
      padding: 0;
    }
    
    .nav-item {
      margin: 0 5px;
    }
  `]
})
export class AppComponent {
  title = 'Student Data Pipeline';
}