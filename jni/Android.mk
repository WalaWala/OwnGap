LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE	:= v8_base
LOCAL_SRC_FILES := ../staticlibs/libv8_base.a
LOCAL_EXPORT_C_INCLUDES := ../include/

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE	:= v8_nosnapshot
LOCAL_SRC_FILES :=  ../staticlibs/libv8_nosnapshot.a
LOCAL_EXPORT_C_INCLUDES := ../include/
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := owngap
LOCAL_SRC_FILES := owngap.cpp Canvas.cpp lodepng.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include

LOCAL_LDLIBS    := -llog -landroid -lGLESv1_CM -ldl
LOCAL_SHARED_LIBRARIES := libowngap
LOCAL_STATIC_LIBRARIES := v8_base v8_nosnapshot

include $(BUILD_SHARED_LIBRARY)
