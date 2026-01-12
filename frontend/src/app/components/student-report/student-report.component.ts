import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Student, PagedResponse, StudentStats } from '../../models/student.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-student-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Student Report</h2>
      <p class="text-muted">View, search, and export student records from the database</p>

      <!-- Statistics -->
      <div *ngIf="stats" class="row mb-3">
        <div class="col-md-2">
          <div class="stat-card">
            <h5>{{stats.total | number}}</h5>
            <small>Total Students</small>
          </div>
        </div>
        <div class="col-md-2" *ngFor="let class of getClassNames()">
          <div class="stat-card">
            <h5>{{getClassCount(class) | number}}</h5>
            <small>{{class}}</small>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="row mb-3">
        <div class="col-md-4">
          <label for="studentIdFilter">Search by Student ID:</label>
          <input
            type="number"
            id="studentIdFilter"
            class="form-control"
            [(ngModel)]="filters.studentId"
            placeholder="Enter student ID"
            (keyup.enter)="loadStudents()"
          />
        </div>
        <div class="col-md-4">
          <label for="classFilter">Filter by Class:</label>
          <select
            id="classFilter"
            class="form-control"
            [(ngModel)]="filters.className"
            (change)="onFilterChange()"
          >
            <option value="">All Classes</option>
            <option value="Class1">Class1</option>
            <option value="Class2">Class2</option>
            <option value="Class3">Class3</option>
            <option value="Class4">Class4</option>
            <option value="Class5">Class5</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="pageSize">Records per page:</label>
          <select
            id="pageSize"
            class="form-control"
            [(ngModel)]="pageSize"
            (change)="onPageSizeChange()"
          >
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </div>
      </div>

      <!-- Actions -->
      <div class="row mb-3">
        <div class="col-md-8">
          <button
            type="button"
            class="btn btn-primary me-2"
            (click)="loadStudents()"
            [disabled]="isLoading"
          >
            <span *ngIf="isLoading">Searching...</span>
            <span *ngIf="!isLoading">Search</span>
          </button>
          <button
            type="button"
            class="btn btn-secondary"
            (click)="clearFilters()"
            [disabled]="isLoading"
          >
            Clear Filters
          </button>
        </div>
        <div class="col-md-4 text-end">
          <div class="dropdown d-inline-block">
            <button
              class="btn btn-success dropdown-toggle"
              type="button"
              id="exportDropdown"
              [disabled]="isLoading"
              (click)="toggleExportDropdown()"
            >
              Export
            </button>
            <ul class="dropdown-menu" [class.show]="showExportDropdown">
              <li><a class="dropdown-item" href="#" (click)="exportData('excel')">Export to Excel</a></li>
              <li><a class="dropdown-item" href="#" (click)="exportData('csv')">Export to CSV</a></li>
              <li><a class="dropdown-item" href="#" (click)="exportData('pdf')">Export to PDF</a></li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Error message -->
      <div *ngIf="errorMessage" class="alert alert-error">
        <h5>✗ Error</h5>
        <p>{{errorMessage}}</p>
      </div>

      <!-- Loading indicator -->
      <div *ngIf="isLoading" class="loading">
        <p>Loading students...</p>
      </div>

      <!-- Students table -->
      <div *ngIf="!isLoading && students && students.length > 0">
        <div class="table-responsive">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Student ID</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Date of Birth</th>
                <th>Class</th>
                <th>Score</th>
                <th>Created At</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let student of students">
                <td>{{student.id}}</td>
                <td>{{student.studentId}}</td>
                <td>{{student.firstName}}</td>
                <td>{{student.lastName}}</td>
                <td>{{student.dob}}</td>
                <td>{{student.className}}</td>
                <td>{{student.score}}</td>
                <td>{{formatDateTime(student.createdAt)}}</td>
                <td>
                  <button
                    type="button"
                    class="btn btn-danger btn-sm"
                    (click)="deleteStudent(student.id!)"
                    [disabled]="isDeleting"
                    title="Delete student"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <small class="text-muted">
              Showing {{getStartRecord()}} to {{getEndRecord()}} of {{totalElements | number}} students
            </small>
          </div>
          <div class="pagination">
            <button
              type="button"
              (click)="goToPage(currentPage - 1)"
              [disabled]="currentPage === 0 || isLoading"
            >
              Previous
            </button>
            
            <button
              type="button"
              *ngFor="let page of getVisiblePages()"
              (click)="goToPage(page)"
              [class.active]="page === currentPage"
              [disabled]="isLoading"
            >
              {{page + 1}}
            </button>
            
            <button
              type="button"
              (click)="goToPage(currentPage + 1)"
              [disabled]="currentPage >= totalPages - 1 || isLoading"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      <!-- No data message -->
      <div *ngIf="!isLoading && students && students.length === 0" class="text-center py-4">
        <p class="text-muted">No students found matching your criteria.</p>
        <button type="button" class="btn btn-primary" (click)="clearFilters()">
          Show All Students
        </button>
      </div>

      <!-- Export progress -->
      <div *ngIf="isExporting" class="alert alert-info">
        <p class="mb-0">Preparing export... Please wait.</p>
      </div>
    </div>
  `,
  styles: [`
    .row {
      display: flex;
      flex-wrap: wrap;
      margin: 0 -15px;
    }
    
    .col-md-2, .col-md-4, .col-md-8 {
      padding: 0 15px;
      flex: 1;
    }
    
    .col-md-2 {
      max-width: 16.666667%;
    }
    
    .col-md-4 {
      max-width: 33.333333%;
    }
    
    .col-md-8 {
      max-width: 66.666667%;
    }
    
    .stat-card {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      text-align: center;
      border: 1px solid #dee2e6;
    }
    
    .stat-card h5 {
      margin: 0 0 5px 0;
      color: #007bff;
      font-size: 1.5rem;
    }
    
    .stat-card small {
      color: #6c757d;
      font-size: 0.8rem;
    }
    
    .table-responsive {
      overflow-x: auto;
    }
    
    .dropdown {
      position: relative;
    }
    
    .dropdown-menu {
      position: absolute;
      top: 100%;
      left: 0;
      z-index: 1000;
      display: none;
      min-width: 10rem;
      padding: 0.5rem 0;
      margin: 0;
      font-size: 0.875rem;
      color: #212529;
      text-align: left;
      background-color: #fff;
      background-clip: padding-box;
      border: 1px solid rgba(0,0,0,.15);
      border-radius: 0.25rem;
      box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,.075);
    }
    
    .dropdown-menu.show {
      display: block;
    }
    
    .dropdown-item {
      display: block;
      width: 100%;
      padding: 0.25rem 1rem;
      clear: both;
      font-weight: 400;
      color: #212529;
      text-decoration: none;
      white-space: nowrap;
      background-color: transparent;
      border: 0;
      cursor: pointer;
    }
    
    .dropdown-item:hover {
      background-color: #f8f9fa;
    }
    
    .d-flex {
      display: flex;
    }
    
    .justify-content-between {
      justify-content: space-between;
    }
    
    .align-items-center {
      align-items: center;
    }
    
    .text-end {
      text-align: right;
    }
    
    .d-inline-block {
      display: inline-block;
    }
    
    .py-4 {
      padding-top: 1.5rem;
      padding-bottom: 1.5rem;
    }
    
    .dropdown-toggle::after {
      content: '';
      display: inline-block;
      margin-left: 0.255em;
      vertical-align: 0.255em;
      border-top: 0.3em solid;
      border-right: 0.3em solid transparent;
      border-bottom: 0;
      border-left: 0.3em solid transparent;
    }
  `]
})
export class StudentReportComponent implements OnInit, OnDestroy {
  students: Student[] = [];
  stats: StudentStats | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;
  
  // Filters
  filters = {
    studentId: null as number | null,
    className: '' as string
  };
  
  // UI state
  isLoading = false;
  isExporting = false;
  isDeleting = false;
  errorMessage: string | null = null;
  showExportDropdown = false;
  
  private subscription: Subscription = new Subscription();

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadStudents();
    this.loadStats();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  loadStudents(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.subscription.add(
      this.apiService.getStudents(
        this.currentPage,
        this.pageSize,
        this.filters.studentId || undefined,
        this.filters.className || undefined
      ).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            const pagedData = response.data;
            this.students = pagedData.content;
            this.totalElements = pagedData.totalElements;
            this.totalPages = pagedData.totalPages;
            this.currentPage = pagedData.number;
          } else {
            this.errorMessage = response.message;
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Failed to load students';
          console.error('Load students error:', error);
        }
      })
    );
  }

  loadStats(): void {
    this.subscription.add(
      this.apiService.getStudentStats().subscribe({
        next: (response) => {
          if (response.success) {
            this.stats = response.data;
          }
        },
        error: (error) => {
          console.error('Load stats error:', error);
        }
      })
    );
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadStudents();
    }
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadStudents();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadStudents();
  }

  clearFilters(): void {
    this.filters.studentId = null;
    this.filters.className = '';
    this.currentPage = 0;
    this.loadStudents();
  }

  exportData(format: 'excel' | 'csv' | 'pdf'): void {
    this.showExportDropdown = false;
    this.isExporting = true;

    const fileName = `students-report.${format === 'excel' ? 'xlsx' : format}`;

    this.subscription.add(
      this.apiService.exportStudents(
        format,
        this.filters.studentId || undefined,
        this.filters.className || undefined,
        fileName
      ).subscribe({
        next: (blob) => {
          this.isExporting = false;
          this.apiService.downloadFile(blob, fileName);
        },
        error: (error) => {
          this.isExporting = false;
          this.errorMessage = `Failed to export ${format.toUpperCase()} file`;
          console.error('Export error:', error);
        }
      })
    );
  }

  toggleExportDropdown(): void {
    this.showExportDropdown = !this.showExportDropdown;
  }

  deleteStudent(id: number): void {
    if (!confirm('Are you sure you want to delete this student? This action cannot be undone.')) {
      return;
    }

    this.isDeleting = true;
    this.errorMessage = null;

    this.subscription.add(
      this.apiService.deleteStudent(id).subscribe({
        next: (response) => {
          this.isDeleting = false;
          if (response.success) {
            // Reload the current page
            this.loadStudents();
            this.loadStats();
          } else {
            this.errorMessage = response.message || 'Failed to delete student';
          }
        },
        error: (error) => {
          this.isDeleting = false;
          this.errorMessage = error.error?.message || 'Failed to delete student. Please try again.';
          console.error('Delete error:', error);
        }
      })
    );
  }

  getClassNames(): string[] {
    return ['Class1', 'Class2', 'Class3', 'Class4', 'Class5'];
  }

  getClassCount(className: string): number {
    if (!this.stats) return 0;
    return (this.stats.byClass as any)[className] || 0;
  }

  getStartRecord(): number {
    return this.currentPage * this.pageSize + 1;
  }

  getEndRecord(): number {
    const end = (this.currentPage + 1) * this.pageSize;
    return Math.min(end, this.totalElements);
  }

  getVisiblePages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages - 1, start + maxVisible - 1);
    
    if (end - start < maxVisible - 1) {
      start = Math.max(0, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  formatDateTime(dateTime: string | undefined): string {
    if (!dateTime) return '';
    
    try {
      const date = new Date(dateTime);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    } catch {
      return dateTime;
    }
  }
}