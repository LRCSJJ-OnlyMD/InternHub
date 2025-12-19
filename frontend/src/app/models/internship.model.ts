export interface Sector {
  id: number;
  name: string;
  code?: string;
  description?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface InternshipRequest {
  title: string;
  description?: string;
  companyName: string;
  companyAddress?: string;
  startDate: string; // ISO date string
  endDate: string;   // ISO date string
  sectorId: number;
}

export interface InternshipResponse {
  id: number;
  title: string;
  description?: string;
  companyName: string;
  companyAddress?: string;
  startDate: string;
  endDate: string;
  status: InternshipStatus;
  studentId: number;
  studentName: string;
  studentEmail: string;
  instructorId?: number;
  instructorName?: string;
  sectorId: number;
  sectorName: string;
  hasReport: boolean;
  refusalComment?: string;
  createdAt: Date;
  updatedAt: Date;
  submittedAt?: Date;
  validatedAt?: Date;
}

export enum InternshipStatus {
  DRAFT = 'DRAFT',
  PENDING_VALIDATION = 'PENDING_VALIDATION',
  VALIDATED = 'VALIDATED',
  REFUSED = 'REFUSED'
}

export interface RefusalRequest {
  refusalComment: string;
}

export interface StatisticsResponse {
  label: string;
  count: number;
}
