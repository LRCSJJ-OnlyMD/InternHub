import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatisticsResponse } from '../models/internship.model';
import { environment } from '../../environments/environment';

export interface EnhancedStatistics {
  totalInternships: number;
  activeInternships: number;
  completedInternships: number;
  pendingInternships: number;
  rejectedInternships: number;
  totalStudents: number;
  totalInstructors: number;
  studentsWithInternships: number;
  instructorsWithInternships: number;
  averageInternshipDuration: number;
  completionRate: number;
  approvalRate: number;
  rejectionRate: number;
  internshipsOverTime: TimeSeriesData[];
  completionsOverTime: TimeSeriesData[];
  internshipsBySector: StatisticsResponse[];
  internshipsByStatus: StatisticsResponse[];
  topSectors: TopPerformerData[];
  topCompanies: TopPerformerData[];
  topInstructors: TopPerformerData[];
  weeklyTrend: TrendData;
  monthlyTrend: TrendData;
}

export interface TimeSeriesData {
  period: string;
  count: number;
  date: string;
}

export interface TopPerformerData {
  name: string;
  count: number;
  percentage: number;
}

export interface TrendData {
  currentPeriod: number;
  previousPeriod: number;
  changePercentage: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
}

export interface InstructorStatistics {
  totalAssignedInternships: number;
  activeInternships: number;
  completedInternships: number;
  pendingValidation: number;
  averageValidationTime: number;
  completionRate: number;
  totalComments: number;
  internshipsByStatus: StatisticsResponse[];
  internshipsBySector: StatisticsResponse[];
  totalStudentsSupervised: number;
  topStudents: StudentPerformanceData[];
}

export interface StudentPerformanceData {
  studentId: number;
  studentName: string;
  internshipsCompleted: number;
  currentStatus: string;
}

export interface StudentStatistics {
  totalInternships: number;
  completedInternships: number;
  activeInternships: number;
  pendingInternships: number;
  currentInternship?: InternshipSummary;
  completionRate: number;
  totalDaysInterned: number;
  sectorsExplored: string[];
  internshipTimeline: InternshipTimelineData[];
  daysUntilCompletion: number;
  progressPercentage: number;
}

export interface InternshipSummary {
  internshipId: number;
  title: string;
  companyName: string;
  status: string;
  startDate: string;
  endDate: string;
  instructorName?: string;
}

export interface InternshipTimelineData {
  internshipId: number;
  title: string;
  companyName: string;
  status: string;
  startDate: string;
  endDate: string;
  duration: number;
}

/**
 * Service for statistics operations.
 */
@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private apiUrl = `${environment.apiUrl}/statistics`;

  constructor(private http: HttpClient) {}

  getInternshipsByStatus(): Observable<StatisticsResponse[]> {
    return this.http.get<StatisticsResponse[]>(`${this.apiUrl}/by-status`);
  }

  getInternshipsBySector(): Observable<StatisticsResponse[]> {
    return this.http.get<StatisticsResponse[]>(`${this.apiUrl}/by-sector`);
  }

  getInternshipsByStatusAndSector(): Observable<StatisticsResponse[]> {
    return this.http.get<StatisticsResponse[]>(`${this.apiUrl}/by-status-and-sector`);
  }
  
  getEnhancedStatistics(): Observable<EnhancedStatistics> {
    return this.http.get<EnhancedStatistics>(`${this.apiUrl}/enhanced`);
  }
  
  getInstructorStatistics(instructorId: number): Observable<InstructorStatistics> {
    return this.http.get<InstructorStatistics>(`${this.apiUrl}/instructor/${instructorId}`);
  }
  
  getStudentStatistics(studentId: number): Observable<StudentStatistics> {
    return this.http.get<StudentStatistics>(`${this.apiUrl}/student/${studentId}`);
  }
}
