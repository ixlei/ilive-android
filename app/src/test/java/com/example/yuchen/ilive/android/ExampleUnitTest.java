package com.example.yuchen.ilive.android;

import android.provider.Settings;
import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    FlashVideoMux flashVideoMux = new FlashVideoMux();

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void flashVideoHeader() {
        byte[] header = flashVideoMux.muxHeader(2);
        byte[] expectHeader = new byte[]{'F', 'L', 'V', 0x01, 0x04, 0x00, 0x00, 0x00, 0x09};
        for(int i = 0; i < header.length; i++) {
            assertEquals(header[i], expectHeader[i]);
        }
    }
}