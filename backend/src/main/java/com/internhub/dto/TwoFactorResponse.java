package com.internhub.dto;

public class TwoFactorResponse {

    private String qrCodeUrl;
    private String secret;
    private String message;

    public TwoFactorResponse() {
    }

    public TwoFactorResponse(String qrCodeUrl, String secret, String message) {
        this.qrCodeUrl = qrCodeUrl;
        this.secret = secret;
        this.message = message;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
