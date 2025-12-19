import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private currentLanguage = 'en';

  constructor(private translate: TranslateService) {
    // Load stored language
    const storedLang = localStorage.getItem('language');
    if (storedLang) {
      this.currentLanguage = storedLang;
    }
  }

  /**
   * Get current language
   */
  getCurrentLanguage(): string {
    return this.currentLanguage;
  }

  /**
   * Change application language
   */
  setLanguage(lang: string): void {
    this.translate.use(lang);
    this.currentLanguage = lang;
    localStorage.setItem('language', lang);
    
    // Update HTML lang attribute for accessibility
    document.documentElement.lang = lang;
  }

  /**
   * Get available languages
   */
  getAvailableLanguages(): { code: string; name: string }[] {
    return [
      { code: 'en', name: 'English' },
      { code: 'fr', name: 'Français' }
    ];
  }

  /**
   * Get translation for a key
   */
  instant(key: string, params?: any): string {
    return this.translate.instant(key, params);
  }
}
