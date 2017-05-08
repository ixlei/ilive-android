package com.example.yuchen.ilive.android;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by yuchen on 17/5/8.
 */

public class SendQueue {
    private int number = 4096;
    private int size = 0;
    private int offset = 0;
    private byte[] prevFrames = null;
    private BlockingQueue<byte[]> framesItem = new LinkedBlockingQueue<>(number);

    public byte[] readPacket(int buffSize) {
        synchronized (this) {
            int result = 0;
            ByteBuffer bb = ByteBuffer.allocate(buffSize);

            if (size >= buffSize) {
                for (; ; ) {
                    try {
                        byte[] temp = prevFrames != null ? prevFrames : framesItem.take();
                        if(temp.length - offset + result < buffSize) {
                            bb.put(temp, offset, temp.length - offset);
                            result += temp.length - offset;
                            offset = 0;
                        } else {
                            bb.put(temp, offset, buffSize - result);
                            bb.position(0);
                            offset = 0;
                            if(temp.length - offset != buffSize - result) {
                                offset += buffSize - result;
                                prevFrames = temp;
                            }
                            return bb.array();
                        }
                    } catch (InterruptedException e) {
                        Log.i("get frame", e.getMessage());
                    }
                }

            }
            return new byte[0];
        }
    }

    public void addFrames(byte[] frames) {
        try {
            framesItem.put(frames);
            size += frames.length;
        } catch (InterruptedException e) {
            Log.i("put frame", e.getMessage());
        }

    }

}
