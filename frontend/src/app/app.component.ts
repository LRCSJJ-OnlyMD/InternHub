import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`
})
export class AppComponent implements OnInit {
  title = 'InternHub';

  constructor(private translate: TranslateService) {
    // Set up available languages
    this.translate.addLangs(['en', 'fr']);
    
    // Set default language
    this.translate.setDefaultLang('en');
  }

  ngOnInit() {
    // Try to use stored language preference or browser language
    const storedLang = localStorage.getItem('language');
    const browserLang = this.translate.getBrowserLang();
    const langToUse = storedLang || (browserLang?.match(/en|fr/) ? browserLang : 'en');
    
    this.translate.use(langToUse);
  }
}
