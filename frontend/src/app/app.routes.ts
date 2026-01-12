import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/generate', pathMatch: 'full' },
  { 
    path: 'generate', 
    loadComponent: () => import('./components/generate-data/generate-data.component').then(m => m.GenerateDataComponent)
  },
  { 
    path: 'process', 
    loadComponent: () => import('./components/process-excel/process-excel.component').then(m => m.ProcessExcelComponent)
  },
  { 
    path: 'upload', 
    loadComponent: () => import('./components/upload-csv/upload-csv.component').then(m => m.UploadCsvComponent)
  },
  { 
    path: 'report', 
    loadComponent: () => import('./components/student-report/student-report.component').then(m => m.StudentReportComponent)
  },
  { path: '**', redirectTo: '/generate' }
];