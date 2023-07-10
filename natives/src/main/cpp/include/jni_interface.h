#ifndef JNI_INTERFACE_H
#define JNI_INTERFACE_H
#include <jni.h>
#include <iostream>
#include <list>

#include "logging.h"
#include "chemmisol.h"

#define JINT "I"
#define JDOUBLE "D"
#define JBOOLEAN "Z"
#define JSTRING "Ljava/lang/String;"
#define JITERATOR "Ljava/util/Iterator;"
#define JPHASE "Lummisco/gama/chemmisol/Phase;"

jint JNI_OnLoad(JavaVM *vm, void *reserved);

class JNIInterface {
	private:
		JNIEnv* env;

		std::unordered_map<std::string, chemmisol::Phase> map_phase;

		
	public:
		JNIInterface(JNIEnv* env);

		std::string GetEnumerator(const char* class_name, const char* enumerator_name) const;

		jobject GetStaticObjectField(
				const char* class_name, const char* name, const char* signature) const;

		jobject CallObjectMethod(
				jobject obj, const char* name, const char* signature) const;

		std::string CallStringMethod(
				jobject obj, const char* name, const char* signature) const;

		double CallDoubleMethod(
				jobject obj, const char* name, const char* signature) const;

		long int CallIntMethod(
				jobject obj, const char* name, const char* signature) const;

		bool CallBooleanMethod(
				jobject obj, const char* name, const char* signature) const;

		std::list<jobject> CallListMethod(
				jobject obj, const char* name, const char* signature) const;

		chemmisol::Phase CallPhaseMethod(
				jobject obj, const char* name, const char* signature) const;
};

#endif
