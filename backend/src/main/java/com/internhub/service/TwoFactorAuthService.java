package com.internhub.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class TwoFactorAuthService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    public String generateSecretKey() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    public String generateQRUrl(String email, String secret) {
        try {
            String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                    "InternHub",
                    email,
                    new GoogleAuthenticatorKey.Builder(secret).build()
            );

            // Generate QR code as base64 image
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            String base64QRCode = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64QRCode;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public boolean validateCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    public int getCurrentCode(String secret) {
        return googleAuthenticator.getTotpPassword(secret);
    }
}
