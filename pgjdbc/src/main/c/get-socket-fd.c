#include "org_herodbx_util_GetSocketFd.h"

JNIEXPORT jint JNICALL Java_org_herodbx_util_GetSocketFd_getSocketFd
  (JNIEnv *env, jobject _this, jobject object_socket){
    jclass clazz_socket = (*env)->GetObjectClass(env, object_socket);

    jfieldID field_socketImpl = (*env)->GetFieldID(env, clazz_socket, "impl", "Ljava/net/SocketImpl;");
    jobject object_socketImpl = (*env)->GetObjectField(env, object_socket, field_socketImpl);

    jclass clazz_socketImpl = (*env)->GetObjectClass(env, object_socketImpl);
    jmethodID method_getFileDescriptor = (*env)->GetMethodID(env,clazz_socketImpl,"getFileDescriptor","()Ljava/io/FileDescriptor;");

    jobject object_fd = (*env)->CallObjectMethod(env,object_socketImpl,method_getFileDescriptor);
    jclass clazz_fd = (*env)->GetObjectClass(env,object_fd);
    jfieldID filed_fd = (*env)->GetFieldID(env,clazz_fd,"fd","I");
    jint fd = (*env)->GetIntField(env,object_fd,filed_fd);

    //printf("fd: %d\n", fd);
    return fd;
  }