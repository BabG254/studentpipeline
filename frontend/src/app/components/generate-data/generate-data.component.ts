import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { GenerateExcelRequest, FileOperationResponse } from '../../models/student.model';
import { Subscription, interval } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';

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
        <p>Generating Excel file with {{request.records | number}} records...</p>
        <p class="text-muted">This may take a few minutes for large datasets.</p>
      </div>

      <!-- Progress Modal -->
      <div *ngIf="showProgress" class="progress-overlay">
        <div class="progress-modal">
          <h3>Generating Data...</h3>
          
          <div class="progress-info">
            <div class="info-row">
              <span class="label">Records:</span>
              <span class="value">{{progressData.currentRecords | number}} / {{progressData.totalRecords | number}}</span>
            </div>
            <div class="info-row">
              <span class="label">Time Elapsed:</span>
              <span class="value">{{formatTime(progressData.elapsedTimeMs)}}</span>
            </div>
            <div class="info-row">
              <span class="label">Status:</span>
              <span class="value">{{progressData.message}}</span>
            </div>
          </div>

          <div class="progress-bar-container">
            <div class="progress-bar-fill" [style.width.%]="getProgressPercentage()"></div>
            <div class="progress-percentage">{{getProgressPercentage()}}%</div>
          </div>
        </div>
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

    .progress-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }

    .progress-modal {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
      min-width: 400px;
      max-width: 600px;
    }

    .progress-modal h3 {
      margin: 0 0 20px 0;
      color: #333;
      text-align: center;
    }

    .progress-info {
      margin-bottom: 20px;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #eee;
    }

    .info-row .label {
      font-weight: bold;
      color: #666;
    }

    .info-row .value {
      color: #333;
    }

    .progress-bar-container {
      position: relative;
      width: 100%;
      height: 30px;
      background: #e9ecef;
      border-radius: 15px;
      overflow: hidden;
    }

    .progress-bar-fill {
      height: 100%;
      background: linear-gradient(90deg, #007bff, #0056b3);
      transition: width 0.3s ease;
    }

    .progress-percentage {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-weight: bold;
      color: #333;
      font-size: 14px;
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

  // Progress tracking
  showProgress = false;
  operationId: string | null = null;
  progressData = {
    currentRecords: 0,
    totalRecords: 0,
    elapsedTimeMs: 0,
    message: 'Initializing...',
    completed: false
  };

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
    this.showProgress = true;
    this.progressData = {
      currentRecords: 0,
      totalRecords: this.request.records || 0,
      elapsedTimeMs: 0,
      message: 'Starting generation...',
      completed: false
    };

    // Clean up fileName if empty
    if (!this.request.fileName?.trim()) {
      this.request.fileName = undefined;
    }

    this.subscription.add(
      this.apiService.generateExcel(this.request).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.operationId = response.data;
            // Start polling for progress
            this.startProgressPolling();
          } else {
            this.isLoading = false;
            this.showProgress = false;
            this.errorMessage = response.message;
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.showProgress = false;
          this.errorMessage = error.error?.message || 'Failed to start Excel generation. Please try again.';
          console.error('Excel generation error:', error);
        }
      })
    );
  }

  startProgressPolling(): void {
    if (!this.operationId) return;

    this.subscription.add(
      interval(10000) // Poll every 10 seconds
        .pipe(
          switchMap(() => this.apiService.getGenerationProgress(this.operationId!)),
          takeWhile((response) => {
            if (response.success && response.data) {
              this.progressData = {
                currentRecords: response.data.currentRecords || 0,
                totalRecords: response.data.totalRecords || this.request.records || 0,
                elapsedTimeMs: response.data.elapsedTimeMs || 0,
                message: response.data.message || 'Processing...',
                completed: response.data.completed || false
              };
              
              // Continue polling while not completed
              return !response.data.completed;
            }
            return false;
          }, true)
        )
        .subscribe({
          complete: () => {
            this.isLoading = false;
            this.showProgress = false;
            if (this.progressData.completed) {
              this.successMessage = `Successfully generated ${this.progressData.totalRecords.toLocaleString()} records in ${this.formatTime(this.progressData.elapsedTimeMs)}`;
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.showProgress = false;
            this.errorMessage = 'Failed to track progress. Please check if generation completed.';
            console.error('Progress polling error:', error);
          }
        })
    );
  }

  getProgressPercentage(): number {
    if (this.progressData.totalRecords === 0) return 0;
    return Math.round((this.progressData.currentRecords / this.progressData.totalRecords) * 100);
  }

  formatTime(ms: number): string {
    if (ms < 1000) return `${ms}ms`;
    const seconds = Math.floor(ms / 1000);
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
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