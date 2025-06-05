
#include <jni.h>
#include <android/log.h>
#include <cstring>
#include <malloc.h>
#include "mpg123.h"
#include <jni.h>
#include <mpg123.h>
#include <vector>
#include <cmath>
#include <algorithm>

#ifdef __cplusplus
extern "C" {
#endif

#define LOG_TAG "MPG123"
#define LOGD(format, args...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##args);

typedef struct _MP3File {
    mpg123_handle *handle;
    bool headerParsed;
    int channels;
    long samplerate;
    int bitrate;
    long num_samples;
    int samples_per_frame;
    double secs_per_frame;
    long total_frames_num;
    double duration;
} MP3File;


MP3File *mp3file_init(mpg123_handle *handle) {
    auto mp3file = (MP3File *) malloc(sizeof(MP3File));
    memset(mp3file, 0, sizeof(MP3File));
    mp3file->handle = handle;
    return mp3file;
}

int mp3file_determineStats(MP3File *mp3) {
    int err = -1;
    if (!mp3)
        return err;

    int encoding;
    mpg123_handle *mh = mp3->handle;
    err = mpg123_getformat(mh, &mp3->samplerate, &mp3->channels, &encoding);
    if (err == MPG123_NEED_MORE)
        return err;
    if (err != MPG123_OK) {
        return err;
    }

    mp3->headerParsed = true;
    mpg123_format_none(mh);
    mpg123_format(mh, mp3->samplerate, mp3->channels, encoding);
    mpg123_frameinfo frameinfo;
    mp3->bitrate = mpg123_info(mh, &frameinfo);
    mp3->bitrate = frameinfo.bitrate;
    mp3->num_samples = mpg123_length(mh);
    mp3->samples_per_frame = mpg123_spf(mh);
    mp3->secs_per_frame = mpg123_tpf(mh);

    if (mp3->num_samples == MPG123_ERR || mp3->samples_per_frame <= 0)
        mp3->total_frames_num = 0;
    else
        mp3->total_frames_num = mp3->num_samples / mp3->samples_per_frame;

    if (mp3->num_samples == MPG123_ERR || mp3->samples_per_frame <= 0 || mp3->secs_per_frame < 0)
        mp3->duration = 0;
    else
        mp3->duration = (double) mp3->num_samples / mp3->samples_per_frame * mp3->secs_per_frame;
    return err;
}

JNIEXPORT jlong JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_init(JNIEnv *env, jobject clazz) {
    mpg123_init();
    int err = MPG123_OK;
    mpg123_handle *mh = mpg123_new(nullptr, &err);
    if (err != MPG123_OK) {
        return 0;
    }

    err = mpg123_open_feed(mh);
    if (err != MPG123_OK) {
        return 0;
    }

    MP3File *stream = mp3file_init(mh);
    return (jlong) stream;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_decode(JNIEnv *env, jobject clazz, jlong handle,
                                                                    jbyteArray jMp3Buf, jint jbytes,
                                                                    jshortArray jRawBufL, jshortArray jRawBufR) {
    if (!handle || !jMp3Buf || !jRawBufL || !jRawBufR) {
        return -1;
    }

    auto *mp3 = (MP3File *) handle;
    mpg123_handle *mh = mp3->handle;

    jbyte *jMp3Buffer = env->GetByteArrayElements(jMp3Buf, nullptr);

    int bufLen = env->GetArrayLength(jRawBufL);
    auto *rawBufL = new short[bufLen];
    auto *rawBufR = new short[bufLen];

    env->GetShortArrayRegion(jRawBufL, 0, bufLen, rawBufL);
    env->GetShortArrayRegion(jRawBufR, 0, bufLen, rawBufR);

    int err;
    off_t frame_offset;
    unsigned char *audio;
    size_t done;

    mpg123_feed(mh, reinterpret_cast<unsigned char *>(jMp3Buffer), jbytes);
    do {
        err = mpg123_decode_frame(mh, &frame_offset, &audio, &done);
        switch (err) {
            case MPG123_NEW_FORMAT: {
                mp3file_determineStats(mp3);
                break;
            }
            case MPG123_NEED_MORE: {
                break;
            }
            case MPG123_OK: {
                if (env->GetArrayLength(jRawBufL) < done / 2)
                    jRawBufL = env->NewShortArray(done / 2);
                if (env->GetArrayLength(jRawBufR) < done / 2)
                    jRawBufR = env->NewShortArray(done / 2);
                short *c_arrayL = env->GetShortArrayElements(jRawBufL, nullptr);
                short *c_arrayR = env->GetShortArrayElements(jRawBufR, nullptr);
                memcpy(c_arrayL, audio, done);
                memcpy(c_arrayR, audio, done);
                env->ReleaseShortArrayElements(jRawBufL, c_arrayL, 0);
                env->ReleaseShortArrayElements(jRawBufR, c_arrayR, 0);
                return done / 2;
            }
            default:
                return 0;
        }
    } while (done > 0);

    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_isParsed(JNIEnv *env, jobject clazz,
                                                                      jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        if (!mp3->headerParsed)
            mp3file_determineStats(mp3);
        return (jboolean) mp3->headerParsed;
    }
    return (jboolean) false;
}

JNIEXPORT jboolean JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_isStereo(JNIEnv *env, jobject clazz,
                                                                      jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        if (mp3->channels == 0)
            mp3file_determineStats(mp3);
        return (jboolean) (mp3->channels == MPG123_STEREO);
    }
    return (jboolean) false;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getSampleRate(JNIEnv *env, jobject clazz,
                                                                           jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        if (mp3->samplerate == 0)
            mp3file_determineStats(mp3);
        return mp3->samplerate;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getBitrate(JNIEnv *env, jobject clazz,
                                                                        jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        if (mp3->bitrate == 0)
            mp3file_determineStats(mp3);
        return mp3->bitrate;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getFramesCount(JNIEnv *env, jobject clazz,
                                                                            jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        return mp3->total_frames_num;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getFrameSize(JNIEnv *env, jobject clazz,
                                                                          jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        return mp3->samples_per_frame;
    }
    return 0;
}
JNIEXPORT void JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_close(JNIEnv *env, jobject clazz,
                                                                   jlong handle) {
    if (handle) {
        auto *mp3 = (MP3File *) handle;
        mpg123_close(mp3->handle);
        mpg123_delete(mp3->handle);
        mpg123_exit();
        free(mp3);
    }
}
#ifdef __cplusplus
}
#endif

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getDuration(JNIEnv *env, jobject thiz, jstring filePath) {
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    mpg123_handle *mh = mpg123_new(nullptr, nullptr);

    if (mh == nullptr) {
        return -1;
    }

    if (mpg123_open(mh, path) != MPG123_OK) {
        mpg123_delete(mh);
        return -1;
    }

    long rate;
    int channels, encoding;
    mpg123_getformat(mh, &rate, &channels, &encoding);

    off_t length = mpg123_length(mh);
    mpg123_delete(mh);

    return static_cast<jdouble>(length) / rate;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_shashluchok_audiorecorder_audio_codec_mpg123_Mpg123_getVolumePeaks(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jstring filePath,
                                                                            jint numPeaks) {
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    mpg123_handle *mh = mpg123_new(nullptr, nullptr);
    if (mh == nullptr) {
        env->ReleaseStringUTFChars(filePath, path);
        return nullptr;
    }

    mpg123_param(mh, MPG123_ADD_FLAGS, MPG123_FORCE_FLOAT, 0.0);
    if (mpg123_open(mh, path) != MPG123_OK) {
        mpg123_delete(mh);
        env->ReleaseStringUTFChars(filePath, path);
        return nullptr;
    }

    long rate;
    int channels, encoding;
    mpg123_getformat(mh, &rate, &channels, &encoding);

    if (encoding != MPG123_ENC_FLOAT_32) {
        mpg123_format_none(mh);
        mpg123_format(mh, rate, channels, MPG123_ENC_FLOAT_32);
    }

    off_t totalSamples = mpg123_length(mh);
    if (totalSamples <= 0 || numPeaks <= 0) {
        mpg123_delete(mh);
        env->ReleaseStringUTFChars(filePath, path);
        return nullptr;
    }

    off_t samplesPerBin = totalSamples / numPeaks;
    std::vector<float> peaks(numPeaks, 0.0f);

    const size_t bufferSize = 8192;
    std::vector<float> buffer(bufferSize / sizeof(float));
    size_t bytesRead = 0;
    off_t sampleIndex = 0;
    int currentBin = 0;
    float currentMax = 0.0f;

    while (true) {
        int err = mpg123_read(mh, (unsigned char *) buffer.data(), buffer.size() * sizeof(float), &bytesRead);
        if (err != MPG123_OK && err != MPG123_DONE) break;

        size_t samplesRead = bytesRead / sizeof(float);
        for (size_t i = 0; i < samplesRead; i += channels) {
            float sample = 0.0f;
            for (int ch = 0; ch < channels; ++ch) {
                sample += std::fabs(buffer[i + ch]);
            }
            sample /= channels;

            currentMax = std::max(currentMax, sample);
            sampleIndex++;

            if (sampleIndex >= samplesPerBin) {
                if (currentBin < numPeaks)
                    peaks[currentBin] = currentMax;
                currentBin++;
                sampleIndex = 0;
                currentMax = 0.0f;
                if (currentBin >= numPeaks) break;
            }
        }

        if (currentBin >= numPeaks || err == MPG123_DONE) break;
    }

    mpg123_delete(mh);
    env->ReleaseStringUTFChars(filePath, path);

    for (float &val: peaks) {
        val = std::min(val + 0.2f, 1.0f);
    }

    jfloatArray result = env->NewFloatArray(numPeaks);
    env->SetFloatArrayRegion(result, 0, numPeaks, peaks.data());
    return result;

    return result;
}