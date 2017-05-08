#include <stdio.h>
#include "libavformat/avformat.h"
#include <libavfilter/avfilter.h>
#include "libavcodec/avcodec.h"
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



 JNIEXPORT jstring Java_com_example_yuchen_ilive_android_LiveActivity_avcodecMsg(JNIEnv* env, jobject obj)
{
    char s[1000] = { 0 };
    sprintf(s, "%s\n", "hello ffmpeg world");
    return (*env)->NewStringUTF(env, s);
 }
