LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-proto-files-under,src)

LOCAL_JAVA_LIBRARIES := com.android.location.provider android-support-v4

LOCAL_PACKAGE_NAME := FreeNetworkLocation
LOCAL_CERTIFICATE := platform
LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)
