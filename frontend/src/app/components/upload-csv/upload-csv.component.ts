import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { FileOperationResponse } from '../../models/student.model';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-upload-csv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Upload CSV to Database</h2>
      <p class="text-muted">Upload a CSV file and save student records to the PostgreSQL database</p>

      <div class="form-group">
        <label for="csvFile">Select CSV File:</label>
        <input
          type="file"
          id="csvFile"
          class="form-control"
          accept=".csv"
          (change)="onFileSelected($event)"
          [disabled]="isLoading"
        />
        <small class="text-muted">Supported formats: .csv</small>
      </div>

      <div *ngIf="selectedFile" class="mb-3">
        <p><strong>Selected File:</strong> {{selectedFile.name}}</p>
        <p><strong>Size:</strong> {{formatFileSize(selectedFile.size)}}</p>
      </div>

      <button
        type="button"
        class="btn btn-primary"
        (click)="uploadCsv()"
        [disabled]="!selectedFile || isLoading"
      >
        <span *ngIf="isLoading">Uploading...</span>
        <span *ngIf="!isLoading">Upload to Database (+5 Score)</span>
      </button>

      <!-- Progress bar -->
      <div *ngIf="isLoading && uploadProgress > 0" class="progress">
        <div
          class="progress-bar"
          [style.width.%]="uploadProgress"
          role="progressbar"
          [attr.aria-valuenow]="uploadProgress"
          aria-valuemin="0"
          aria-valuemax="100"
        >
          {{uploadProgress}}%
        </div>
      </div>

      <!-- Loading indicator -->
      <div *ngIf="isLoading" class="loading">
        <p>Uploading CSV to database...</p>
        <p class="text-muted">Processing records in batches and calculating final scores</p>
      </div>

      <!-- Success message -->
      <div *ngIf="successMessage" class="alert alert-success">
        <h5>✓ Upload Successful!</h5>
        <p>{{successMessage}}</p>
        <p *ngIf="uploadResult">
          <strong>Records Inserted:</strong> {{uploadResult.recordsProcessed | number}}<br>
          <strong>Operation:</strong> {{uploadResult.operation}}
        </p>
        <button
          type="button"
          class="btn btn-success mt-2"
          (click)="goToReport()"
        >
          View Student Report
        </button>
      </div>

      <!-- Error message -->
      <div *ngIf="errorMessage" class="alert alert-error">
        <h5>✗ Upload Failed</h5>
        <p>{{errorMessage}}</p>
      </div>

      <!-- Instructions -->
      <div class="mt-3">
        <h5>Instructions:</h5>
        <ol>
          <li>Select a CSV file with student data (preferably generated from "Process Excel" page)</li>
          <li>Click "Upload to Database" to save records to PostgreSQL</li>
          <li>The system will process records in batches for optimal performance</li>
          <li>Final database scores will be calculated as: <strong>Original Excel Score + 5</strong></li>
          <li>Duplicate student IDs will be skipped automatically</li>
        </ol>
      </div>

      <!-- CSV format info -->
      <div class="mt-3">
        <h5>Expected CSV Format:</h5>
        <div class="alert alert-info">
          <p><strong>Required Columns (in order):</strong></p>
          <code>studentId,firstName,lastName,DOB,class,score</code>
          <p class="mt-2 mb-0">
            <strong>Example:</strong><br>
            <code>1,John,Doe,2005-01-15,Class1,75</code><br>
            <code>2,Jane,Smith,2005-03-22,Class2,68</code>
          </p>
        </div>
      </div>

      <!-- Score calculation info -->
      <div class="mt-3">
        <div class="alert alert-info">
          <h6>Score Calculation Logic:</h6>
          <p class="mb-0">
            The system automatically detects the score format:<br>
            • <strong>Original Excel scores (55-75):</strong> Add +5 → Final: 60-80<br>
            • <strong>Processed CSV scores (65-85):</strong> Subtract 10, then add 5 → Final: 60-80<br>
            • <strong>Result:</strong> Database always stores Original Excel Score + 5
          </p>
        </div>
      </div>

      <!-- Performance info -->
      <div class="mt-3">
        <small class="text-muted">
          <strong>Performance Features:</strong><br>
          • Batch processing (5,000 records per batch)<br>
          • Transaction management for data integrity<br>
          • Duplicate detection and skipping<br>
          • Memory-efficient streaming for large files
        </small>
      </div>
    </div>
  `,
  styles: [`
    .progress {
      margin: 15px 0;
    }
    
    .loading {
      text-align: center;
      padding: 20px;
      margin: 20px 0;
      background-color: #f8f9fa;
      border-radius: 4px;
    }
    
    .alert-info {
      background-color: #d1ecf1;
      border-color: #bee5eb;
      color: #0c5460;
      padding: 15px;
      border-radius: 4px;
    }
    
    code {
      background-color: #f8f9fa;
      padding: 2px 4px;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 0.9em;
    }
  `]
})
export class UploadCsvComponent implements OnInit, OnDestroy {
  selectedFile: File | null = null;
  isLoading = false;
  uploadProgress = 0;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  uploadResult: FileOperationResponse | null = null;

  private subscription: Subscription = new Subscription();

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.clearMessages();
    
    // Subscribe to upload progress
    this.subscription.add(
      this.apiService.uploadProgress$.subscribe(progress => {
        this.uploadProgress = progress;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    this.apiService.resetProgress();
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      if (file.type === 'text/csv' || file.name.endsWith('.csv')) {
        this.selectedFile = file;
        this.clearMessages();
      } else {
        this.errorMessage = 'Please select a valid CSV file (.csv)';
        this.selectedFile = null;
      }
    }
  }

  uploadCsv(): void {
    if (!this.selectedFile || this.isLoading) return;

    this.clearMessages();
    this.isLoading = true;
    this.uploadProgress = 0;
    this.apiService.resetProgress();

    this.subscription.add(
      this.apiService.uploadCsvToDatabase(this.selectedFile)
        .pipe(filter(response => response !== null))
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            if (response.success) {
              this.successMessage = response.message;
              this.uploadResult = response.data;
            } else {
              this.errorMessage = response.message;
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = error.error?.message || 'Failed to upload CSV to database. Please try again.';
            console.error('CSV upload error:', error);
          }
        })
    );
  }

  goToReport(): void {
    this.router.navigate(['/report']);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  private clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
    this.uploadResult = null;
  }
}