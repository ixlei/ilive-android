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

        byte[] header = flashVideoMux.muxTagHeader(9, 1680, 16, 0, 0);
        byte[] res = flashVideoMux.convertIntToByte(1680);
        System.out.println(res[3]);
        System.out.println((int)res[2]);
        System.out.println((int)res[1]);
        System.out.println((byte)1680);
        ByteBuffer bb = ByteBuffer.allocate(11);
        bb.putInt(1222);
        bb.putInt(37833);
        bb.put((byte)0);
        bb.put((byte)0);
        bb.put((byte)0);
        //bb.put((byte)0);


        byte[] expectHeader = new byte[]{0x09, 0x00, res[2], res[3], 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00};
        //assertArrayEquals(header, expectHeader);
        System.out.print(ByteBuffer.allocate(4).putInt(1680).limit());
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