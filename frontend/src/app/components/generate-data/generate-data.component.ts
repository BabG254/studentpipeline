import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { GenerateExcelRequest, FileOperationResponse } from '../../models/student.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-generate-data',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Generate Excel Data</h2>
      <p class="text-muted">Generate Excel file with random student data for testing</p>

      <form (ngSubmit)="generateExcel()" #form="ngForm">
        <div class="form-group">
          <label for="records">Number of Records:</label>
          <input
            type="number"
            id="records"
            name="records"
            class="form-control"
            [(ngModel)]="request.records"
            [min]="1"
            [max]="10000000"
            required
            [disabled]="isLoading"
          />
          <small class="text-muted">Enter number of student records to generate (1 - 10,000,000)</small>
        </div>

        <div class="form-group">
          <label for="fileName">File Name (optional):</label>
          <input
            type="text"
            id="fileName"
            name="fileName"
            class="form-control"
            [(ngModel)]="request.fileName"
            placeholder="e.g., students-data.xlsx"
            [disabled]="isLoading"
          />
          <small class="text-muted">Leave empty to auto-generate filename</small>
        </div>

        <button
          type="submit"
          class="btn btn-primary"
          [disabled]="!form.valid || isLoading"
        >
          <span *ngIf="isLoading">Generating...</span>
          <span *ngIf="!isLoading">Generate Excel</span>
        </button>
      </form>

      <!-- Loading indicator -->
      <div *ngIf="isLoading" class="loading">
        <p>Generating Excel file with {{request.records}} records...</p>
        <p class="text-muted">This may take a few minutes for large datasets.</p>
      </div>

      <!-- Success message -->
      <div *ngIf="successMessage" class="alert alert-success">
        <h5>✓ Excel Generated Successfully!</h5>
        <p>{{successMessage}}</p>
        <p *ngIf="generatedFile">
          <strong>File:</strong> {{generatedFile.fileName}}<br>
          <strong>Path:</strong> {{generatedFile.path}}<br>
          <strong>Records:</strong> {{generatedFile.recordsProcessed | number}}
        </p>
      </div>

      <!-- Error message -->
      <div *ngIf="errorMessage" class="alert alert-error">
        <h5>✗ Generation Failed</h5>
        <p>{{errorMessage}}</p>
      </div>

      <!-- Quick presets -->
      <div class="mt-3">
        <h5>Quick Presets:</h5>
        <button
          type="button"
          class="btn btn-outline-primary me-2"
          (click)="setPreset(1000)"
          [disabled]="isLoading"
        >
          1K Records
        </button>
        <button
          type="button"
          class="btn btn-outline-primary me-2"
          (click)="setPreset(10000)"
          [disabled]="isLoading"
        >
          10K Records
        </button>
        <button
          type="button"
          class="btn btn-outline-primary me-2"
          (click)="setPreset(100000)"
          [disabled]="isLoading"
        >
          100K Records
        </button>
        <button
          type="button"
          class="btn btn-outline-primary"
          (click)="setPreset(1000000)"
          [disabled]="isLoading"
        >
          1M Records
        </button>
      </div>

      <!-- Performance info -->
      <div class="mt-3">
        <small class="text-muted">
          <strong>Performance Guidelines:</strong><br>
          • 1K records: ~1-2 seconds<br>
          • 10K records: ~5-10 seconds<br>
          • 100K records: ~30-60 seconds<br>
          • 1M records: ~5-10 minutes<br>
          Large datasets are generated using streaming to minimize memory usage.
        </small>
      </div>
    </div>
  `,
  styles: [`
    .me-2 {
      margin-right: 0.5rem;
    }
    
    .btn-outline-primary {
      border: 1px solid #007bff;
      color: #007bff;
      background: transparent;
    }
    
    .btn-outline-primary:hover {
      background-color: #007bff;
      color: white;
    }
    
    .loading {
      text-align: center;
      padding: 20px;
      margin: 20px 0;
      background-color: #f8f9fa;
      border-radius: 4px;
    }
  `]
})
export class GenerateDataComponent implements OnInit, OnDestroy {
  request: GenerateExcelRequest = {
    records: 1000,
    fileName: ''
  };

  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  generatedFile: FileOperationResponse | null = null;

  private subscription: Subscription = new Subscription();

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    // Reset any previous state
    this.clearMessages();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  generateExcel(): void {
    if (this.isLoading) return;

    this.clearMessages();
    this.isLoading = true;

    // Clean up fileName if empty
    if (!this.request.fileName?.trim()) {
      this.request.fileName = undefined;
    }

    this.subscription.add(
      this.apiService.generateExcel(this.request).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = response.message;
            this.generatedFile = response.data;
          } else {
            this.errorMessage = response.message;
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Failed to generate Excel file. Please try again.';
          console.error('Excel generation error:', error);
        }
      })
    );
  }

  setPreset(records: number): void {
    this.request.records = records;
    this.request.fileName = `students-${records}.xlsx`;
    this.clearMessages();
  }

  private clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
    this.generatedFile = null;
  }
}