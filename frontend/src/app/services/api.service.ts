import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  Student,
  ApiResponse,
  PagedResponse,
  GenerateExcelRequest,
  FileOperationResponse,
  StudentStats
} from '../models/student.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';
  
  // Progress tracking
  private uploadProgressSubject = new BehaviorSubject<number>(0);
  public uploadProgress$ = this.uploadProgressSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Generate Excel file with student data
   */
  generateExcel(request: GenerateExcelRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(
      `${this.baseUrl}/generate-excel`,
      request
    );
  }

  /**
   * Get progress of Excel generation
   */
  getGenerationProgress(operationId: string): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(
      `${this.baseUrl}/generate-excel/progress/${operationId}`
    );
  }

  /**
   * Convert Excel to CSV
   */
  convertExcelToCsv(file: File): Observable<ApiResponse<FileOperationResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ApiResponse<FileOperationResponse>>(
      `${this.baseUrl}/convert-excel-to-csv`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map((event: HttpEvent<ApiResponse<FileOperationResponse>>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            if (event.total) {
              const progress = Math.round((event.loaded / event.total) * 100);
              this.uploadProgressSubject.next(progress);
            }
            return null as any;
          case HttpEventType.Response:
            this.uploadProgressSubject.next(100);
            return event.body!;
          default:
            return null as any;
        }
      })
    );
  }

  /**
   * Upload CSV to database
   */
  uploadCsvToDatabase(file: File): Observable<ApiResponse<FileOperationResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ApiResponse<FileOperationResponse>>(
      `${this.baseUrl}/upload-csv-to-db`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map((event: HttpEvent<ApiResponse<FileOperationResponse>>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            if (event.total) {
              const progress = Math.round((event.loaded / event.total) * 100);
              this.uploadProgressSubject.next(progress);
            }
            return null as any;
          case HttpEventType.Response:
            this.uploadProgressSubject.next(100);
            return event.body!;
          default:
            return null as any;
        }
      })
    );
  }

  /**
   * Get students with pagination and filters
   */
  getStudents(
    page: number = 0,
    size: number = 20,
    studentId?: number,
    className?: string
  ): Observable<ApiResponse<PagedResponse<Student>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }

    if (className) {
      params = params.set('className', className);
    }

    return this.http.get<ApiResponse<PagedResponse<Student>>>(
      `${this.baseUrl}/students`,
      { params }
    );
  }

  /**
   * Get student by student ID
   */
  getStudentByStudentId(studentId: number): Observable<ApiResponse<Student>> {
    return this.http.get<ApiResponse<Student>>(`${this.baseUrl}/students/${studentId}`);
  }

  /**
   * Export students to specified format
   */
  exportStudents(
    format: 'excel' | 'csv' | 'pdf',
    studentId?: number,
    className?: string,
    fileName?: string
  ): Observable<Blob> {
    let params = new HttpParams().set('format', format);

    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }

    if (className) {
      params = params.set('className', className);
    }

    if (fileName) {
      params = params.set('fileName', fileName);
    }

    return this.http.get(`${this.baseUrl}/students/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Get student statistics
   */
  getStudentStats(): Observable<ApiResponse<StudentStats>> {
    return this.http.get<ApiResponse<StudentStats>>(`${this.baseUrl}/students/stats`);
  }

  /**
   * Health check
   */
  healthCheck(): Observable<ApiResponse<string>> {
    return this.http.get<ApiResponse<string>>(`${this.baseUrl}/health`);
  }

  /**
   * Delete student by ID
   */
  deleteStudent(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.baseUrl}/students/${id}`);
  }

  /**
   * Reset progress
   */
  resetProgress(): void {
    this.uploadProgressSubject.next(0);
  }

  /**
   * Download file from blob
   */
  downloadFile(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}