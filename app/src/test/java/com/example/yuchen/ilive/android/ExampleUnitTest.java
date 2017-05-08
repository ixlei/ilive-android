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
//        ByteBuffer bb = ByteBuffer.allocate(30);
//        ByteBuffer b1 = ByteBuffer.allocate(10);
//        byte[] b = new byte[]{1,2};
//        b1.put(b);
//        bb.put((byte)0);
//        bb.put((byte)1);
//        bb.put((byte)2);
//
//
//       // b1.put(bb.array());
//        System.out.println(b1);

//        bb.position(1);
//        bb.limit(3);

//        System.out.println(bb.position() + "_" + bb.capacity() + "_" + bb.limit() + "-" + bb.get(0));
//
//        System.out.println(bb.position() + "_" + bb.capacity() + "_" + bb.limit() + "-" + bb.get());
//        byte[] b = new byte[2];
//        bb.get(b);
//        System.out.print(b[0] + "-" + b[1]);
//        System.out.println(b);

    }

    @Test
    public void flashVideoConvertIntToByte() {
        byte[] b = new byte[]{1, 2, 3, 'a', 4};
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(b, 1, 2);
        bb.put((byte)0);
        System.out.println("dhdhdh");
        System.out.println(bb.array()[0]);

    }


}