import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatMenuModule, MatIconModule, TranslateModule],
  template: `
    <button mat-icon-button [matMenuTriggerFor]="languageMenu" class="language-button">
      <mat-icon>language</mat-icon>
    </button>
    <mat-menu #languageMenu="matMenu">
      <button mat-menu-item 
              *ngFor="let lang of languages" 
              (click)="changeLanguage(lang.code)"
              [class.active]="currentLanguage === lang.code">
        <span>{{ lang.name }}</span>
        <mat-icon *ngIf="currentLanguage === lang.code">check</mat-icon>
      </button>
    </mat-menu>
  `,
  styles: [`
    .language-button {
      margin: 0 8px;
    }
    
    mat-menu-item.active {
      background-color: rgba(0, 0, 0, 0.04);
      font-weight: 500;
    }
    
    mat-menu-item mat-icon {
      margin-left: 8px;
      color: #4caf50;
    }
  `]
})
export class LanguageSwitcherComponent {
  languages = this.languageService.getAvailableLanguages();
  currentLanguage = this.languageService.getCurrentLanguage();

  constructor(private languageService: LanguageService) {}

  changeLanguage(lang: string): void {
    this.languageService.setLanguage(lang);
    this.currentLanguage = lang;
  }
}
