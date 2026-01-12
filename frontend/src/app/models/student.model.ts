export interface Student {
  id?: number;
  studentId: number;
  firstName: string;
  lastName: string;
  dob: string;
  className: string;
  score: number;
  createdAt?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
}

export interface GenerateExcelRequest {
  records: number;
  fileName?: string;
}

export interface FileOperationResponse {
  path: string;
  fileName: string;
  recordsProcessed: number;
  operation: string;
}

export interface StudentStats {
  total: number;
  byClass: {
    Class1: number;
    Class2: number;
    Class3: number;
    Class4: number;
    Class5: number;
  };
}