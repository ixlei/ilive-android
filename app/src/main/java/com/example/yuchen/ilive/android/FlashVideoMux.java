package com.example.yuchen.ilive.android;

import java.nio.ByteBuffer;

/**
 * Created by yuchen on 17/5/4.
 */

public class FlashVideoMux {
    /**
     *
     * @param type enum{0, 1, 2} bit0 -> video, bit1-> audio
     * @return flv headr
     */

    public byte[] muxHeader(int type) {
      //9bytes
        byte[] header = new byte[9];
        // flv flag
        header[0] = 0x46;
        header[1] = 0x4c;
        header[2] = 0x56;

        header[3] = 0x01; // flv version
        header[4] = 0x0;
        if((type & 1) != 0) {
            header[4] |= (header[4] | 0x01);
        }
        if((type & 2) == 2) {
            header[4] |= (header[4] | 0x04);
        }
        header[5] = header[6] = header[7] = 0x0;
        header[8] = 0x09;
        return header;
    }
}
