import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { FileOperationResponse } from '../../models/student.model';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-process-excel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card">
      <h2>Process Excel to CSV</h2>
      <p class="text-muted">Upload an Excel file and convert it to CSV with score adjustment (+10)</p>

      <div class="form-group">
        <label for="excelFile">Select Excel File:</label>
        <input
          type="file"
          id="excelFile"
          class="form-control"
          accept=".xlsx,.xls"
          (change)="onFileSelected($event)"
          [disabled]="isLoading"
        />
        <small class="text-muted">Supported formats: .xlsx, .xls</small>
      </div>

      <div *ngIf="selectedFile" class="mb-3">
        <p><strong>Selected File:</strong> {{selectedFile.name}}</p>
        <p><strong>Size:</strong> {{formatFileSize(selectedFile.size)}}</p>
      </div>

      <button
        type="button"
        class="btn btn-primary"
        (click)="processExcel()"
        [disabled]="!selectedFile || isLoading"
      >
        <span *ngIf="isLoading">Processing...</span>
        <span *ngIf="!isLoading">Convert to CSV (+10 Score)</span>
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
        <p>Converting Excel to CSV...</p>
        <p class="text-muted">Processing file and adjusting scores (+10 to each student's score)</p>
      </div>

      <!-- Success message -->
      <div *ngIf="successMessage" class="alert alert-success">
        <h5>✓ Conversion Successful!</h5>
        <p>{{successMessage}}</p>
        <p *ngIf="processedFile">
          <strong>Output File:</strong> {{processedFile.fileName}}<br>
          <strong>Path:</strong> {{processedFile.path}}<br>
          <strong>Records Processed:</strong> {{processedFile.recordsProcessed | number}}
        </p>
        <button
          type="button"
          class="btn btn-success mt-2"
          (click)="downloadCsv()"
        >
          Download CSV
        </button>
      </div>

      <!-- Error message -->
      <div *ngIf="errorMessage" class="alert alert-error">
        <h5>✗ Conversion Failed</h5>
        <p>{{errorMessage}}</p>
      </div>

      <!-- Instructions -->
      <div class="mt-3">
        <h5>Instructions:</h5>
        <ol>
          <li>Select an Excel file (.xlsx or .xls) generated from the "Generate Data" page</li>
          <li>Click "Convert to CSV" to process the file</li>
          <li>The system will read each row and add +10 to the score column</li>
          <li>Download the resulting CSV file</li>
          <li>Use this CSV file in the "Upload CSV" page to save to database</li>
        </ol>
      </div>

      <!-- Score transformation info -->
      <div class="mt-3">
        <div class="alert alert-info">
          <h6>Score Transformation:</h6>
          <p class="mb-0">
            <strong>Original Excel scores:</strong> 55-75<br>
            <strong>CSV scores (after +10):</strong> 65-85<br>
            <strong>Final DB scores (CSV-10+5):</strong> 60-80 (Original + 5)
          </p>
        </div>
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
  `]
})
export class ProcessExcelComponent implements OnInit, OnDestroy {
  selectedFile: File | null = null;
  isLoading = false;
  uploadProgress = 0;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  processedFile: FileOperationResponse | null = null;

  private subscription: Subscription = new Subscription();

  constructor(private apiService: ApiService) {}

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
      const allowedTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-excel'
      ];
      
      if (allowedTypes.includes(file.type) || file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
        this.selectedFile = file;
        this.clearMessages();
      } else {
        this.errorMessage = 'Please select a valid Excel file (.xlsx or .xls)';
        this.selectedFile = null;
      }
    }
  }

  processExcel(): void {
    if (!this.selectedFile || this.isLoading) return;

    this.clearMessages();
    this.isLoading = true;
    this.uploadProgress = 0;
    this.apiService.resetProgress();

    this.subscription.add(
      this.apiService.convertExcelToCsv(this.selectedFile)
        .pipe(filter(response => response !== null))
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            if (response.success) {
              this.successMessage = response.message;
              this.processedFile = response.data;
            } else {
              this.errorMessage = response.message;
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = error.error?.message || 'Failed to convert Excel to CSV. Please try again.';
            console.error('Excel to CSV conversion error:', error);
          }
        })
    );
  }

  downloadCsv(): void {
    if (!this.processedFile) return;
    
    // For now, show message that file is saved on server
    alert(`CSV file has been saved to: ${this.processedFile.path}\n\nIn a production environment, this would trigger a file download.`);
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
    this.processedFile = null;
  }
}