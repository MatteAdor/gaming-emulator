/**
 * adrenobox_native.h
 * Low-level C bridge interface for AdrenoBox Android NDK.
 */

#ifndef ADRENOBOX_NATIVE_H
#define ADRENOBOX_NATIVE_H

#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <stdbool.h>
#include <sys/types.h>

#define LOG_TAG "AdrenoBoxNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

// Container process launch parameters
typedef struct {
    const char *rootfs_path;       // Path to glibc / rootfs directory
    const char *wine_prefix;       // e.g., /data/data/com.example/files/prefixes/default
    const char *wine_bin;          // Path to wine64 binary
    const char *box64_bin;         // Path to box64 binary
    const char *turnip_icd_json;   // Path to turnip ICD configuration
    const char *executable_path;   // Path to target .exe
    const char *working_dir;       // Working directory
    int dynarec_bigblock;          // 0, 1, 2
    int dynarec_fastnan;           // 0 or 1
    int enable_esync;              // 1 = enabled
    int enable_fsync;              // 1 = enabled
} ContainerConfig;

// Process handle and execution status
typedef struct {
    pid_t child_pid;
    int stdout_pipe[2];
    int stderr_pipe[2];
    bool is_running;
} WineProcessContext;

// Native exported functions
JNIEXPORT jint JNICALL Java_com_example_core_NativeBridge_launchWineSession(
    JNIEnv *env,
    jobject thiz,
    jstring jRootfs,
    jstring jWinePrefix,
    jstring jExecutable,
    jstring jTurnipIcd,
    jint dynarecLevel
);

JNIEXPORT jint JNICALL Java_com_example_core_NativeBridge_terminateSession(
    JNIEnv *env,
    jobject thiz
);

JNIEXPORT jint JNICALL Java_com_example_core_NativeBridge_getProcessStatus(
    JNIEnv *env,
    jobject thiz
);

JNIEXPORT jstring JNICALL Java_com_example_core_NativeBridge_getDriverInfo(
    JNIEnv *env,
    jobject thiz
);

JNIEXPORT void JNICALL Java_com_example_core_NativeBridge_sendInputEvent(
    JNIEnv *env,
    jobject thiz,
    jint eventType,
    jint keyCode,
    jfloat x,
    jfloat y
);

#ifdef __cplusplus
}
#endif

#endif // ADRENOBOX_NATIVE_H
