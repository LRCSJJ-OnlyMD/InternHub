import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sector } from '../models/internship.model';
import { environment } from '../../environments/environment';

/**
 * Service for sector operations.
 */
@Injectable({
  providedIn: 'root'
})
export class SectorService {
  private apiUrl = `${environment.apiUrl}/admin/sectors`;

  constructor(private http: HttpClient) {}

  getAllSectors(): Observable<Sector[]> {
    return this.http.get<Sector[]>(this.apiUrl);
  }

  getSectorById(id: number): Observable<Sector> {
    return this.http.get<Sector>(`${this.apiUrl}/${id}`);
  }

  createSector(sector: { name: string; code?: string; description?: string }): Observable<Sector> {
    return this.http.post<Sector>(this.apiUrl, sector);
  }

  updateSector(id: number, sector: { name: string; code?: string; description?: string }): Observable<Sector> {
    return this.http.put<Sector>(`${this.apiUrl}/${id}`, sector);
  }

  deleteSector(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
