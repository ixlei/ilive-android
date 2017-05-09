#include <stdio.h>
#include "libavformat/avformat.h"
#include <libavfilter/avfilter.h>
#include "libavcodec/avcodec.h"
#include <libavutil/mathematics.h>
#include <libavutil/time.h>
#include <sys/queue.h>
#include <jni.h>

JNIEXPORT jstring Java_com_example_yuchen_ilive_android_LiveActivity_avcodecInfo(JNIEnv* env, jobject obj)
{
    char info[4000] = { 0 };
    int count = 100;  //输出前100个codec名字
    
    av_register_all();//初始化所有decoder和encoder,注册所有容器类型和codec
    char s[1000] = { 0 };
    sprintf(s, "%s\n", avcodec_configuration());
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL && count > 0){
        //输出解码器和编码器
        if(c_temp->decode != NULL){
            sprintf(info,"%s[Decoder]",info);
        }
        else{
            sprintf(info,"%s[Enc]",info);
        }
        
        sprintf(info,"%s[%10s]\n",info,c_temp->name);
        
        c_temp = c_temp->next;
        count--;
    }

    return (*env)->NewStringUTF(env, info);
 }

jclass sendQueueClass;
jobject jobj;

static JavaVM *jvm;

void init(JNIEnv* env) {
   jint rs = (*env)->GetJavaVM(env, &jvm);
}


int read_packet(void *opaque, uint8_t *buf, int buf_size) {
    JNIEnv *env;
    jint rs = (*jvm)->AttachCurrentThread(jvm, &env, NULL);
    jmethodID midReadPacket = (*env)->GetMethodID(env, sendQueueClass, "readPacket", "(I)[B");
    jbyteArray framesArray = (jbyteArray)(*env)->CallObjectMethod(env, jobj, midReadPacket, buf_size);
    jsize len = (*env)->GetArrayLength(env, framesArray);
    fprintf(stdout, "ssss");
    if(len == buf_size) {
        jbyte* framePointer = (*env)->GetByteArrayElements(env, framesArray, NULL);
        memcpy(buf, framePointer, buf_size);
        return buf_size;
    }
    return 0;
}

JNIEXPORT jstring Java_com_example_yuchen_ilive_android_PackerAudioAndVideo_initFramesQueue(JNIEnv* env, jobject obj, jobject sendQueueObject) {
    init(env);
    jobj = (jobject)(*env)->NewGlobalRef(env, sendQueueObject);
    sendQueueClass = (jclass)(*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, sendQueueObject));
}

jclass j;
jobject empObjectt;

JNIEXPORT jint Java_com_example_yuchen_ilive_android_LiveActivity_init(JNIEnv* env, jobject obj, jobject empObject) {
  j = (jclass)(*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, empObject));
  empObjectt = (jobject)(*env)->NewGlobalRef(env, empObject);
  jmethodID get = (*env)->GetMethodID(env, j, "get", "()I");
  return (int)(*env)->CallIntMethod(env, empObject, get);
}

JNIEXPORT jint Java_com_example_yuchen_ilive_android_LiveActivity_ini(JNIEnv* env, jobject obj) {
  jmethodID get = (*env)->GetMethodID(env, j, "get", "()I");
  return (int)(*env)->CallIntMethod(env, empObjectt, get);
}


JNIEXPORT jstring Java_com_example_yuchen_ilive_android_PackerAudioAndVideo_pushFlvStream(JNIEnv* env, jobject obj) {
    int ret = 0, size = 4096;
    int64_t start_time = 0;
    int videoindex = -1;  
    int frame_index = 0;
    int buff_size = sizeof(uint8_t) * size;
    uint8_t* input_buffer = (uint8_t*)av_mallocz(buff_size);
    char log[100] = { 0 };
    const char* server_url = "rtmp://127.0.0.1";

    AVPacket pkt;
    AVIOContext* avio_ctx = NULL;
    AVOutputFormat* output_format = NULL;
    AVFormatContext* input_format_ctx = NULL;
    AVFormatContext* output_format_ctx = NULL;

    av_register_all();
    avformat_network_init();

    input_format_ctx = avformat_alloc_context();
    if(!input_format_ctx) {
        ret = AVERROR(ENOMEM);
        sprintf(log, "%s\n", "init error");
        return (*env)->NewStringUTF(env, log);
    }
    avio_ctx = avio_alloc_context(input_buffer, buff_size, 0, NULL, read_packet, NULL, NULL);
    if (!avio_ctx) {
        ret = AVERROR(ENOMEM);
        sprintf(log, "%s\n", "init error");
        return (*env)->NewStringUTF(env, log);
    }

    input_format_ctx->pb = avio_ctx;
    ret = avformat_open_input(&input_format_ctx, NULL, NULL, NULL);
    if(ret < 0) {
        sprintf(log, "%s\n", "init error");
        return (*env)->NewStringUTF(env, log);
    }

    avformat_alloc_output_context2(&output_format_ctx, NULL, "flv", server_url);
    if(!output_format_ctx) {
       fprintf(stderr, "Could not open context2\n");
       sprintf(log, "%s\n", "init error");
       return (*env)->NewStringUTF(env, log);
    }

    output_format = output_format_ctx->oformat;  
    for (int i = 0; i < input_format_ctx->nb_streams; i++) {  
        //根据输入流创建输出流（Create output AVStream according to input AVStream）  
        AVStream *in_stream = input_format_ctx->streams[i];  
        AVStream *out_stream = avformat_new_stream(output_format_ctx, in_stream->codec->codec);  
        if (!out_stream) {  
            printf( "Failed allocating output stream\n");  
            ret = AVERROR_UNKNOWN;  
            sprintf(log, "%s\n", "init error");
            return (*env)->NewStringUTF(env, log);
        }  
        //复制AVCodecContext的设置（Copy the settings of AVCodecContext）  
        ret = avcodec_copy_context(out_stream->codec, in_stream->codec);  
        if (ret < 0) {  
            printf( "Failed to copy context from input to output stream codec context\n");  
            goto end;  
        }  
        out_stream->codec->codec_tag = 0;  
        if (output_format_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
            out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
        }
    }
    av_dump_format(output_format_ctx, 0, server_url, 1);
    if (!(output_format_ctx->flags & AVFMT_NOFILE)) {  
        ret = avio_open(&output_format_ctx->pb, server_url, AVIO_FLAG_WRITE);  
        if (ret < 0) {  
            printf( "Could not open output URL '%s'", server_url);  
            goto end;  
        } 
    }
    //写文件头（Write file header）  
    ret = avformat_write_header(output_format_ctx, NULL);  
    if (ret < 0) {  
        printf( "Error occurred when opening output URL\n");  
        goto end;  
    } 
    start_time=av_gettime();  
    while (1) {  
        AVStream *in_stream, *out_stream;
        //获取一个AVPacket（Get an AVPacket）  
        ret = av_read_frame(input_format_ctx, &pkt);  
        if (ret < 0)  
            break;  
        //FIX：No PTS (Example: Raw H.264)  
        //Simple Write PTS  
        // if(pkt.pts==AV_NOPTS_VALUE){  
        //     //Write PTS  
        //     AVRational time_base1=ifmt_ctx->streams[videoindex]->time_base;  
        //     //Duration between 2 frames (us)  
        //     int64_t calc_duration=(double)AV_TIME_BASE/av_q2d(ifmt_ctx->streams[videoindex]->r_frame_rate);  
        //     //Parameters  
        //     pkt.pts=(double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE);  
        //     pkt.dts=pkt.pts;  
        //     pkt.duration=(double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE);  
        // }  
        //Important:Delay  
        if(pkt.stream_index==videoindex){  
            AVRational time_base=input_format_ctx->streams[videoindex]->time_base;  
            AVRational time_base_q={1,AV_TIME_BASE};  
            int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);  
            int64_t now_time = av_gettime() - start_time;  
            if (pts_time > now_time)  
                av_usleep(pts_time - now_time);  
  
        }  
  
        in_stream  = input_format_ctx->streams[pkt.stream_index];  
        out_stream = output_format_ctx->streams[pkt.stream_index];  
        /* copy packet */  
        //转换PTS/DTS（Convert PTS/DTS）  
        // pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));  
        // pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));  
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);  
        pkt.pos = -1;  
        //Print to Screen  
        if(pkt.stream_index == videoindex){  
            printf("Send %8d video frames to output URL\n",frame_index);  
            frame_index++;  
        }  
        //ret = av_write_frame(ofmt_ctx, &pkt);  
        ret = av_interleaved_write_frame(output_format_ctx, &pkt);  
  
        if (ret < 0) {  
            printf( "Error muxing packet\n");  
            break;  
        }  
          
        av_free(&pkt);  
          
    }  
    //写文件尾（Write file trailer）  
    av_write_trailer(output_format_ctx); 

end: 
    //free
    avformat_free_context(input_format_ctx);
    avformat_free_context(output_format_ctx);
    av_free(avio_ctx);

    sprintf(log, "%s\n", "hello ffmpeg world");
    return (*env)->NewStringUTF(env, log);


}