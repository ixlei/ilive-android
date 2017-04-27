package com.example.yuchen.ilive.android;

/**
 * Created by yuchen on 17/4/27.
 */

public class ExceptionClass {
    public static class CameraNotSupportException extends Exception {
        public CameraNotSupportException(String message) {
            super(message);
        }
    }
}
