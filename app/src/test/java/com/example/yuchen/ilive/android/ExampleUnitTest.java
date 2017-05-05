package com.example.yuchen.ilive.android;

import android.provider.Settings;
import android.util.Log;

import org.junit.Test;

import java.nio.ByteBuffer;

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

    @Test
    public void falshVideoTagHeader() {
//
    }

    @Test
    public void flashVideoConvertIntToByte() {
        byte[] res = flashVideoMux.convertIntToByte(0);
        for(int i = 0; i < 4; i++) {
          //  assertEquals(res[i], 0x00);
        }
        res = flashVideoMux.convertIntToByte(18);
        byte[] expectArr = new byte[] {0x00,0x00, 0x00, 0x12};
        //assertArrayEquals(res, expectArr);

    }

}