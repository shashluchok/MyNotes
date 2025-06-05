#include <jni.h>
#include <android/log.h>
#include <cstring>
#include "lame.h"

#ifdef __cplusplus
extern "C" {
#endif

#define LOG_TAG "LAME"
#define LOGD(format, args...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##args);

lame_t initLame(int sampleRate) {
    LOGD("Init parameters");
    lame_t lame = lame_init();
    lame_set_in_samplerate(lame, sampleRate);
    lame_set_VBR(lame, vbr_off);
    lame_set_num_channels(lame, 1);
    lame_init_params(lame);
    LOGD("Sample rate: %d", sampleRate);
    return lame;
}

JNIEXPORT jlong JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_lame_Lame_initLameEncoder(JNIEnv *env, jclass clazz, jint jSampleRate) {
    return (long) initLame(jSampleRate);
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_lame_Lame_encode(JNIEnv *env, jclass clazz, jlong jLame,
                                                          jshortArray jPcmBuf, jint jSamples,
                                                          jbyteArray jMp3Buf) {
    if (!jLame || !jPcmBuf || !jMp3Buf) {
        return -1;
    }

    auto lame = (lame_t)jLame;

    jshort *jPcmBuffer = env->GetShortArrayElements(jPcmBuf, NULL);

    int bufLen = env->GetArrayLength(jMp3Buf);
    auto* mp3Buf = new unsigned char[bufLen];
    env->GetByteArrayRegion(jMp3Buf, 0, bufLen, reinterpret_cast<jbyte*>(mp3Buf));

    int result = lame_encode_buffer(lame, jPcmBuffer, jPcmBuffer,
                                    jSamples,
                                    mp3Buf, bufLen);

    env->SetByteArrayRegion(jMp3Buf, 0, bufLen, reinterpret_cast<jbyte*>(mp3Buf));

    env->ReleaseShortArrayElements(jPcmBuf, jPcmBuffer, 0);
    env->ReleaseByteArrayElements(jMp3Buf, reinterpret_cast<jbyte *>(mp3Buf), 0);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_lame_Lame_flush(JNIEnv *env, jclass clazz, jlong jLame,
                                                         jbyteArray jMp3Buf) {
    if (!jLame || !jMp3Buf) {
        return -1;
    }

    auto lame = (lame_t)jLame;

    int bufLen = env->GetArrayLength(jMp3Buf);
    unsigned char* mp3Buf = new unsigned char[bufLen];
    env->GetByteArrayRegion(jMp3Buf, 0, bufLen, reinterpret_cast<jbyte*>(mp3Buf));

    int result = lame_encode_flush(lame, mp3Buf, bufLen);

    env->SetByteArrayRegion(jMp3Buf, 0, bufLen, reinterpret_cast<jbyte*>(mp3Buf));
    env->ReleaseByteArrayElements(jMp3Buf, reinterpret_cast<jbyte *>(mp3Buf), 0);

    return result;
}

JNIEXPORT void JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_lame_Lame_closeLame(JNIEnv *env, jclass clazz, jlong jLame) {
auto lame = (lame_t)jLame;
if (lame) {
lame_close(lame);
lame = NULL;
}
}

JNIEXPORT jstring JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_lame_Lame_getVersion(JNIEnv *env, jclass clazz) {
const char *version = get_lame_version();
return env->NewStringUTF(version);
}
#ifdef __cplusplus
}
#endif