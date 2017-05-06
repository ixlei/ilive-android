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

    /**
     *
     * @param tagType enum{0x08, 0x09, 0x12}
     * @param filter {0x00}
     * @param timestamp
     * @param timestampExtended PTS = timestamp | timestampExtended << 24
     * @return
     */

    public ByteBuffer muxTagHeader(int tagType, int dataLength, int timestamp, int timestampExtended, int filter) {
        ByteBuffer tagHeader = ByteBuffer.allocate(11);
        int headerFirstByte = ((filter << 6) | tagType);
        //frame type and data length
        int dataLenAndType = ((dataLength & 0x00FFFFFF) | ((headerFirstByte & 0xFF) << 24));
        tagHeader.putInt(dataLenAndType);
        //
        int pts = (((timestamp << 8) & 0xFFFFFF00) | (timestampExtended & 0x000000FF));
        tagHeader.putInt(pts);
        //StreamID
        for(int i = 8; i < 11; i++) {
            tagHeader.put((byte)0x00);
        }

        return tagHeader;
    }

    public byte[] convertIntToByte(int value) {
        return ByteBuffer.allocate(4).putInt(1680).array();
    }

    public byte[] muxAudioTag(int soundType, double soudRate, int channelCount, int sampleBit, boolean isSequenceHeader) {
        byte[] audioHeader = new byte[2];
        //if sound type is not aac, audio header have AACPacketType
        if(soundType != 10) {
            audioHeader = new byte[1];
        }

        int soundSize = sampleBit == 8 ? 0 : 1;
        int SoundType = channelCount == 1 ? 0 : 1;

        audioHeader[0] = (byte) (((soundType & 0x0F)<< 4)
                | ((getSoundRate(soudRate) & 0x03) << 2)
                | ((soundSize & 0x01)<< 1)
                | (SoundType & 0x01));
        if(soundType == 10) {
            audioHeader[1] = (byte) (isSequenceHeader ? 0 : 1);
        }
        return audioHeader;
    }

    public byte[] muxSequenceAudioInfo(int sampleRate, int channelCount) {
        int sampleRateIndex = getSampleIndexAudioRate(sampleRate);
        byte[] sequenceAudioInfo = new byte[2];
        sequenceAudioInfo[0] = (byte) (0x10 | ((sampleRateIndex >> 1) & 0x7));
        sequenceAudioInfo[1] = (byte) (((sampleRateIndex & 0x1)<<7) | ((channelCount & 0xF) << 3));
        return sequenceAudioInfo;
    }

    public byte[] muxVideoTagHeader(int frameType, int codecID) {
        byte[] videoTagHeader = new byte[1];
        videoTagHeader[0] = (byte) (((frameType & 0x0F) << 4) | (codecID & 0x0F));
        return videoTagHeader;
    }

    public ByteBuffer muxAVCTagHeader(int frametype, int AVCPacketType, int compositionTime) {
        ByteBuffer avcTagHeader = ByteBuffer.allocate(5);
        avcTagHeader.put(muxVideoTagHeader(frametype, 7));
        int avcPacketTypeAndCompTime = (((AVCPacketType & 0x000000FF) << 24) | (compositionTime & 0x00FFFFFF));
        avcTagHeader.putInt(avcPacketTypeAndCompTime);
        return avcTagHeader;
    }


    /**
     *  http://befo.io/4178.html
     * @param sps h264 sps
     * @param pps h264 pps
     * @return
     */
    public ByteBuffer muxVideoFirstTag(byte[] sps, byte[] pps) {
        ByteBuffer videoFirstTag = ByteBuffer.allocate(11);
        //1
        videoFirstTag.put((byte)0x01);
        videoFirstTag.put(sps[1]);
        videoFirstTag.put(sps[2]);
        videoFirstTag.put(sps[3]);
        videoFirstTag.put((byte)0xFF);

        videoFirstTag.put((byte)0xe1);
        videoFirstTag.putShort((short)sps.length);
        videoFirstTag.put(sps);

        videoFirstTag.put((byte)0x01);
        videoFirstTag.putShort((short)pps.length);
        videoFirstTag.put(pps);
        return videoFirstTag;
    }

    public byte[] muxFlvMetadata(int width, int height, int fps, int audioRate, int audioSize, int channelCout) {
        boolean isStereo = channelCout == 2 ? true : false;
        return new byte[1];
    }

    public int getSoundRate(double val) {
        if(val == 5.5) {
            return 0;
        }
        if(val == 11) {
            return 1;
        }
        if(val == 22) {
            return 2;
        }
        if(val == 44) {
            return 3;
        }
        return 3;
    }

    public int getSampleIndexAudioRate(int sampleRate) {
        switch (sampleRate) {
            case 96000: return 0;
            case 88200: return 1;
            case 64000: return 2;
            case 48000: return 3;
            case 44100 : return 4;
            default: return 4;
        }
    }

}
