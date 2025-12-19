import 'package:flutter/material.dart';

class AppTheme {
  // Modern Gaming Color Palette
  static const Color primaryPurple = Color(0xFF6C63FF);
  static const Color secondaryPink = Color(0xFFFF6B9D);
  static const Color accentOrange = Color(0xFFFFB86C);
  static const Color accentCyan = Color(0xFF00D4FF);
  static const Color successGreen = Color(0xFF00E676);
  static const Color warningYellow = Color(0xFFFFD600);
  static const Color errorRed = Color(0xFFFF5252);
  
  // Dark Theme Colors
  static const Color darkBackground = Color(0xFF0F0E17);
  static const Color darkCard = Color(0xFF1C1B29);
  static const Color darkCardElevated = Color(0xFF252336);
  
  // Light Theme Colors
  static const Color lightBackground = Color(0xFFF8F7FF);
  static const Color lightCard = Color(0xFFFFFFFF);
  
  // Text Colors
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFFB8B5C3);
  static const Color textDark = Color(0xFF0F0E17);
  
  // Gradient Definitions
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [primaryPurple, secondaryPink],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
  
  static const LinearGradient accentGradient = LinearGradient(
    colors: [accentOrange, accentCyan],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
  
  static const LinearGradient successGradient = LinearGradient(
    colors: [Color(0xFF00E676), Color(0xFF00C853)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
  
  // Dark Theme
  static ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: darkBackground,
    colorScheme: ColorScheme.dark(
      primary: primaryPurple,
      secondary: secondaryPink,
      tertiary: accentCyan,
      surface: darkCard,
      background: darkBackground,
      error: errorRed,
    ),
    
    // Card Theme
    cardTheme: CardThemeData(
      color: darkCard,
      elevation: 8,
      shadowColor: primaryPurple.withOpacity(0.3),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
    ),
    
    // AppBar Theme
    appBarTheme: AppBarTheme(
      backgroundColor: darkBackground,
      elevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontSize: 24,
        fontWeight: FontWeight.bold,
        color: textPrimary,
        letterSpacing: 1.2,
      ),
    ),
    
    // Elevated Button Theme
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        padding: EdgeInsets.symmetric(horizontal: 32, vertical: 16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        elevation: 8,
        shadowColor: primaryPurple.withOpacity(0.5),
      ),
    ),
    
    // Input Decoration Theme
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: darkCardElevated,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: textSecondary.withOpacity(0.2), width: 1),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: primaryPurple, width: 2),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: errorRed, width: 1),
      ),
      contentPadding: EdgeInsets.symmetric(horizontal: 20, vertical: 20),
    ),
    
    // Text Theme
    textTheme: TextTheme(
      headlineLarge: TextStyle(
        fontSize: 32,
        fontWeight: FontWeight.bold,
        color: textPrimary,
        letterSpacing: 1.5,
      ),
      headlineMedium: TextStyle(
        fontSize: 24,
        fontWeight: FontWeight.bold,
        color: textPrimary,
        letterSpacing: 1.2,
      ),
      bodyLarge: TextStyle(
        fontSize: 16,
        color: textPrimary,
      ),
      bodyMedium: TextStyle(
        fontSize: 14,
        color: textSecondary,
      ),
    ),
    
    // Icon Theme
    iconTheme: IconThemeData(
      color: textPrimary,
      size: 24,
    ),
  );
  
  // Helper method to create gradient text
  static ShaderMask gradientText({
    required Widget child,
    required Gradient gradient,
  }) {
    return ShaderMask(
      shaderCallback: (bounds) => gradient.createShader(bounds),
      child: child,
    );
  }
  
  // Box Decoration with Glow Effect
  static BoxDecoration glowingBox({
    required Color glowColor,
    double borderRadius = 20,
    Color? backgroundColor,
  }) {
    return BoxDecoration(
      color: backgroundColor ?? darkCard,
      borderRadius: BorderRadius.circular(borderRadius),
      boxShadow: [
        BoxShadow(
          color: glowColor.withOpacity(0.3),
          blurRadius: 20,
          spreadRadius: 2,
        ),
        BoxShadow(
          color: glowColor.withOpacity(0.2),
          blurRadius: 40,
          spreadRadius: 5,
        ),
      ],
    );
  }
  
  // Animated Gradient Button Style
  static BoxDecoration gradientButton({
    required Gradient gradient,
    double borderRadius = 16,
  }) {
    return BoxDecoration(
      gradient: gradient,
      borderRadius: BorderRadius.circular(borderRadius),
      boxShadow: [
        BoxShadow(
          color: primaryPurple.withOpacity(0.5),
          blurRadius: 15,
          offset: Offset(0, 8),
        ),
      ],
    );
  }
}
