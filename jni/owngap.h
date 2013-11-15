#ifndef OWNGAP_H
#define OWNGAP_H

#include <android/log.h>
#define LOG_TAG "owngap_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)

#include <sys/time.h>
#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <stdlib.h>
#include <unistd.h>

#include "Canvas.h"
#include <GLES/gl.h>
#include <v8.h>
#include <v8-debug.h>
using namespace v8;

static int currentTextureId = 0;
int otherTextureCount = 0;

static AAssetManager* assetmanager;
static Persistent<Context> context;
static Isolate* isolate;

static JavaVM *vm = 0;
static jmethodID loadImageJava;
static jmethodID getImageWidthJava;
static jmethodID getImageHeightJava;
static jmethodID getButtonStateJava;
static jmethodID getAxisStateJava;
static jmethodID showCursorJava;
static jmethodID loadSoundJava;
static jmethodID stopSoundJava;
static jmethodID playSoundJava;
static jmethodID setVolumeJava;
static jmethodID getSoundLoadedJava;
static jmethodID isPausedJava;
static jmethodID setOrthoJava;
static jmethodID makeHttpRequestJava;
static jmethodID getHttpResponseJava;
static jmethodID showKeyboardJava;
static jmethodID hideKeyboardJava;
static jmethodID getKeyboardStateJava;
static jmethodID shiftPressedJava;

static jclass mainActivityClass;
static JNIEnv *jniEnv;
static jobject jniObj;
static Persistent<Function> callbackFunction;
static Persistent<Context> debug_message_context;

#endif