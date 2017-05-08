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


int read_packet(void *opaque, uint8_t *buf, int buf_size) {
    jmethodID midReadPacket = (*env)->GetMethodID(env, employeeClass, "readPacket", "(I)[B");

    jbyteArray framesArray = (jbyteArray)(*jenv)->CallVoidMethod(jenv, jobj, midReadPacket, buf_size);
    jsize len = env->GetArrayLength(framesArray);
    if(len == buf_size) {
        jbyte* framePointer = (*env)->GetByteArrayElements(env, framesArray, NULL);
        memcpy(buf, framePointer, buf_size);
        return buf_size;
    }
    return 0;
}

JNIEXPORT jstring Java_com_example_yuchen_ilive_android_LiveActivity_initFramesQueue(JNIEnv* env, jobject obj, jobject sendQueueObject) {
    sendQueueClass = (*env)->GetObjectClass(env, sendQueueObject);
}

JNIEXPORT jstring Java_com_example_yuchen_ilive_android_LiveActivity_pushFlvStream(JNIEnv* env, jobject obj) {
    int ret = 0, size = 4096;
    int buff_size = sizeof(uint8_t) * size;
    uint8_t* input_buffer = (uint8_t*)av_mallocz(buff_size);

    const char* server_url = "rtmp://127.0.0.1";

    AVIOContext* avio_ctx = NULL;
    AVFormatContext* input_format_ctx = NULL;
    AVFormatContext* output_format_ctx = NULL;

    av_register_all();
    avformat_network_init();

    input_format_ctx = avformat_alloc_context();
    if(!input_format_ctx) {
        ret = AVERROR(ENOMEM);
        goto end;
    }
    avio_ctx = avio_alloc_context(input_buffer, buff_size, 0, read_packet, NULL, NULL);
    if (!avio_ctx) {
        ret = AVERROR(ENOMEM);
        goto end;
    }

    input_format_ctx->pb = avio_ctx;
    ret = avformat_open_input(input_format_ctx, NULL, NULL, NULL);
    if(ret < 0) {
        fprintf(stderr, "Could not open input\n");
        goto end;
    }

    avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", out_filename);
    if(!ofmt_ctx) {
       fprintf(stderr, "Could not open context2\n");
       goto end;
    }

    //free
    avformat_free_context(input_format_ctx);
    avformat_free_context(output_format_ctx);
    av_free(pb);

     char s[1000] = { 0 };
     sprintf(s, "%s\n", "hello ffmpeg world");
     return (*env)->NewStringUTF(env, s);


}