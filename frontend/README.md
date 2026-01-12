# Student Data Pipeline - Frontend

An Angular 17 frontend application for the Student Data Pipeline system, providing a user-friendly interface for data generation, processing, and reporting.

## Features

- **Generate Data**: Create Excel files with customizable student records
- **Process Excel**: Upload and convert Excel files to CSV with score adjustments
- **Upload CSV**: Import CSV data into the database with batch processing
- **Student Reports**: View, search, filter, and export student data with pagination
- **Responsive Design**: Mobile-friendly interface using Bootstrap
- **Progress Tracking**: Real-time upload progress indicators
- **Export Capabilities**: Export reports in Excel, CSV, and PDF formats

## Tech Stack

- Angular 17
- TypeScript
- RxJS
- Bootstrap 5
- Angular HTTP Client
- Standalone Components

## Prerequisites

- Node.js 18.19+ 
- npm 8.0+
- Angular CLI 17+

## Installation & Setup

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Backend URL
Edit `src/app/services/api.service.ts` and update the `baseUrl` if needed:
```typescript
private baseUrl = 'http://localhost:8080/api';
```

### 4. Start Development Server
```bash
npm start
# or
ng serve
```

The application will be available at http://localhost:4200

### 5. Build for Production
```bash
npm run build
# or
ng build --configuration production
```

## Project Structure

```
src/
├── app/
│   ├── components/
│   │   ├── generate-data/          # Excel generation component
│   │   ├── process-excel/          # Excel to CSV conversion
│   │   ├── upload-csv/             # CSV to database upload
│   │   └── student-report/         # Student reporting and exports
│   ├── models/
│   │   └── student.model.ts        # TypeScript interfaces
│   ├── services/
│   │   └── api.service.ts          # HTTP API service
│   ├── app.component.ts            # Root component with navigation
│   ├── app.routes.ts               # Application routing
│   └── main.ts                     # Application bootstrap
├── styles.css                      # Global styles
└── index.html                      # Main HTML template
```

### Components Overview

#### 1. Generate Data Component (`/generate`)
- Create Excel files with 1 to 10,000,000 student records
- Configurable filename
- Quick presets (1K, 10K, 100K, 1M records)
- Performance guidelines and progress tracking

#### 2. Process Excel Component (`/process`)
- Upload Excel files (.xlsx, .xls)
- Convert to CSV with +10 score adjustment
- File validation and progress indicators
- Score transformation explanations

#### 3. Upload CSV Component (`/upload`)
- Upload CSV files to PostgreSQL database
- Batch processing with progress tracking
- Duplicate detection and intelligent score calculation
- CSV format validation

#### 4. Student Report Component (`/report`)
- Paginated student listing (10, 20, 50, 100 per page)
- Search by Student ID
- Filter by Class (Class1-Class5)
- Export to Excel, CSV, PDF
- Student statistics dashboard

## API Integration

The frontend communicates with the Spring Boot backend through the `ApiService`:

### Service Methods

```typescript
// Generate Excel
generateExcel(request: GenerateExcelRequest): Observable<ApiResponse<FileOperationResponse>>

// Convert Excel to CSV
convertExcelToCsv(file: File): Observable<ApiResponse<FileOperationResponse>>

// Upload CSV to Database
uploadCsvToDatabase(file: File): Observable<ApiResponse<FileOperationResponse>>

// Get paginated students
getStudents(page, size, studentId?, className?): Observable<ApiResponse<PagedResponse<Student>>>

// Export students
exportStudents(format, studentId?, className?, fileName?): Observable<Blob>

// Get statistics
getStudentStats(): Observable<ApiResponse<StudentStats>>
```

### Progress Tracking

Upload progress is tracked using RxJS observables:

```typescript
// Subscribe to upload progress
this.apiService.uploadProgress$.subscribe(progress => {
  this.uploadProgress = progress;
});
```

## User Interface

### Navigation
- Clean navigation bar with route-based highlighting
- Responsive design for mobile and desktop
- Clear visual indicators for active pages

### Forms and Validation
- Form validation with real-time feedback
- File type validation
- Input sanitization and error handling

### Data Display
- Responsive tables with horizontal scrolling
- Pagination controls with page size options
- Loading states and progress indicators
- Export functionality with format selection

### Styling
- Bootstrap-based responsive design
- Custom CSS for enhanced UX
- Consistent color scheme and typography
- Mobile-first approach

## Configuration

### Environment Configuration
For production deployment, update the API base URL:

```typescript
// In api.service.ts
private baseUrl = 'https://your-production-api.com/api';
```

### Build Configuration
The `angular.json` file includes:
- Bootstrap CSS and JS integration
- Asset management
- Production optimization settings

## Usage Guide

### 1. Generate Sample Data
1. Navigate to "Generate Data"
2. Enter number of records (e.g., 1000)
3. Optionally specify filename
4. Click "Generate Excel"
5. Wait for completion and note the file path

### 2. Process Excel File
1. Navigate to "Process Excel"
2. Upload the generated Excel file
3. Click "Convert to CSV"
4. Download the processed CSV file

### 3. Upload to Database
1. Navigate to "Upload CSV"
2. Upload the processed CSV file
3. Click "Upload to Database"
4. Monitor batch processing progress

### 4. View Reports
1. Navigate to "Student Report"
2. Use filters to search specific students or classes
3. Navigate through pages using pagination
4. Export data in desired format (Excel/CSV/PDF)

## Performance Considerations

### File Upload Optimization
- Chunked upload support for large files
- Progress tracking with HTTP events
- Error handling and retry mechanisms

### Memory Management
- Efficient observable management with proper unsubscription
- Component lifecycle management
- Minimal DOM manipulation

### User Experience
- Loading indicators for all async operations
- Responsive design for various screen sizes
- Clear error messages and user feedback

## Development

### Code Style
- TypeScript strict mode enabled
- Consistent component structure
- Reactive programming with RxJS
- Standalone components architecture

### Testing
```bash
# Run unit tests
npm test

# Run end-to-end tests
npm run e2e

# Run tests with coverage
npm run test:coverage
```

### Linting and Formatting
```bash
# Lint code
npm run lint

# Format code
npm run format
```

## Building and Deployment

### Development Build
```bash
npm run build
```

### Production Build
```bash
npm run build:prod
```

### Docker Deployment
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build:prod

FROM nginx:alpine
COPY --from=build /app/dist/student-data-pipeline-frontend /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Static Hosting
The built application in `dist/` can be deployed to:
- Nginx
- Apache HTTP Server
- AWS S3 + CloudFront
- Netlify
- Vercel

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure backend CORS is configured for frontend origin
2. **File Upload Issues**: Check file size limits and format validation
3. **API Connection**: Verify backend is running and accessible
4. **Build Errors**: Ensure Node.js and npm versions are compatible

### Debug Mode
```bash
ng serve --configuration development --source-map
```

### Network Issues
Check browser developer tools Network tab for API call failures.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests if applicable
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.