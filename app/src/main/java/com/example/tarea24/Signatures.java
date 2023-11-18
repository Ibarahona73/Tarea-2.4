package com.example.tarea24;

import android.graphics.Bitmap;

public class Signatures{
    private String description;
    private Bitmap digitalSignature;

    public Signatures(String description, Bitmap digitalSignature) {
        this.description = description;
        this.digitalSignature = digitalSignature;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getDigitalSignature() {
        return digitalSignature;
    }
}