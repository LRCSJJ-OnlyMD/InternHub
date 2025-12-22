import 'package:flutter/material.dart';

class AppTheme {
  // Professional Color Palette - Matching Frontend
  static const Color primaryBlue = Color(0xFF1e3a8a);
  static const Color primaryBlueDark = Color(0xFF1e40af);
  static const Color primaryBlueLight = Color(0xFF3b82f6);
  static const Color secondaryOrange = Color(0xFFf97316);
  static const Color successGreen = Color(0xFF10b981);
  static const Color warningYellow = Color(0xFFf97316);
  static const Color errorRed = Color(0xFFef4444);
  static const Color infoCyan = Color(0xFF1e3a8a);

  // Backwards compatibility aliases - map old gaming theme to new professional theme
  static const Color primaryPurple = primaryBlue; // Old purple -> New blue
  static const Color secondaryPink = secondaryOrange; // Old pink -> New orange
  static const Color accentOrange = secondaryOrange; // Keep orange
  static const Color accentCyan =
      primaryBlueLight; // Old cyan -> New light blue

  // Dark Theme Colors
  static const Color darkBackground = Color(0xFF0f172a);
  static const Color darkCard = Color(0xFF1e293b);
  static const Color darkCardElevated = Color(0xFF334155);

  // Light Theme Colors
  static const Color lightBackground = Color(0xFFf8fafc);
  static const Color lightCard = Color(0xFFFFFFFF);

  // Text Colors
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFF94a3b8);
  static const Color textDark = Color(0xFF0f172a);

  // Gradient Definitions - Professional Theme
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [primaryBlue, primaryBlueLight],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient accentGradient = LinearGradient(
    colors: [secondaryOrange, Color(0xFFfb923c)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient successGradient = LinearGradient(
    colors: [successGreen, Color(0xFF059669)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  // Dark Theme
  static ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: darkBackground,
    colorScheme: ColorScheme.dark(
      primary: primaryBlue,
      secondary: secondaryOrange,
      tertiary: primaryBlueLight,
      surface: darkCard,
      background: darkBackground,
      error: errorRed,
    ),

    // Card Theme
    cardTheme: CardThemeData(
      color: darkCard,
      elevation: 8,
      shadowColor: primaryBlue.withOpacity(0.3),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
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
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        elevation: 8,
        shadowColor: primaryBlue.withOpacity(0.5),
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
        borderSide: BorderSide(color: primaryBlue, width: 2),
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
      bodyLarge: TextStyle(fontSize: 16, color: textPrimary),
      bodyMedium: TextStyle(fontSize: 14, color: textSecondary),
    ),

    // Icon Theme
    iconTheme: IconThemeData(color: textPrimary, size: 24),
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
          color: primaryBlue.withOpacity(0.5),
          blurRadius: 15,
          offset: Offset(0, 8),
        ),
      ],
    );
  }
}
