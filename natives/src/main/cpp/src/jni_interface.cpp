#include "jni_interface.h"

el::Logger* chemmisol_java_logger = chemmisol::init_logger(CHEM_JAVA_LOGID);

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
/*
 *    el::Configurations logger_conf;
 *    logger_conf.setToDefault();
 *
 *    logger_conf.setGlobally(
 *            el::ConfigurationType::Format, "[chemmisol-cpp][%level] %msg");
 *    logger_conf.setGlobally(
 *            el::ConfigurationType::Filename, "chemmisol-cpp.log");
 *    el::Loggers::reconfigureLogger("default", logger_conf);
 *
 */
	return JNI_VERSION_10;
}

void mapPhase(
		const JNIInterface& interface,
		std::unordered_map<std::string, chemmisol::Phase>& map_phase) {
	map_phase = {
		{interface.GetEnumerator("ummisco/gama/chemmisol/Phase", "SOLVENT"),
			chemmisol::SOLVENT},
		{interface.GetEnumerator("ummisco/gama/chemmisol/Phase", "AQUEOUS"),
			chemmisol::AQUEOUS},
		{interface.GetEnumerator("ummisco/gama/chemmisol/Phase", "MINERAL"),
			chemmisol::MINERAL}
	};
}

std::string JNIInterface::GetEnumerator(
		const char* class_name, const char* enumerator_name) const {
			std::string sig("L");
			sig.append(class_name);
			sig.append(";");
			return CallStringMethod(GetStaticObjectField(
					class_name,
					enumerator_name,
					sig.data()), "name", "()" JSTRING);
		}

JNIInterface::JNIInterface(JNIEnv* env)
	: env(env) {
		mapPhase(*this, map_phase);
	}

std::string JNIInterface::convert(jstring java_str) const {
	jsize n = env->GetStringUTFLength(java_str);
	const char* _chars = env->GetStringUTFChars(java_str, NULL);

	std::string cpp_str(_chars, (std::size_t) n);
	env->ReleaseStringUTFChars(java_str, _chars);
	return cpp_str;
}

jobject JNIInterface::GetStaticObjectField(
		const char* class_name, const char* name, const char* signature) const {
	auto clazz = env->FindClass(class_name);
	return env->GetStaticObjectField(clazz,
			env->GetStaticFieldID(clazz, name, signature)
			);
}

void JNIInterface::CallVoidMethod(
		jobject obj, const char* name, const char* signature, ...) const {
	std::va_list args;
	va_start(args, signature);
	env->CallVoidMethodV(obj,
			env->GetMethodID(env->GetObjectClass(obj), name, signature), args
			);
}

jobject JNIInterface::CallObjectMethod(
		jobject obj, const char* name, const char* signature, ...) const {
	std::va_list args;
	va_start(args, signature);
	return env->CallObjectMethodV(obj,
			env->GetMethodID(env->GetObjectClass(obj), name, signature), args
			);
}

std::string JNIInterface::CallStringMethod(
		jobject obj, const char* name, const char* signature) const {
	jstring java_str = (jstring) env->CallObjectMethod(obj,
			env->GetMethodID(env->GetObjectClass(obj), name, signature)
			);
	return convert(java_str);
}

double JNIInterface::CallDoubleMethod(
		jobject obj, const char* name, const char* signature) const {
	return env->CallDoubleMethod(
			obj,
			env->GetMethodID(
				env->GetObjectClass(obj),
				name, signature
				)
			);
}

long int JNIInterface::CallIntMethod(
		jobject obj, const char* name, const char* signature) const {
	return env->CallIntMethod(
			obj,
			env->GetMethodID(
				env->GetObjectClass(obj),
				name, signature
				)
			);
}

bool JNIInterface::CallBooleanMethod(
		jobject obj, const char* name, const char* signature) const {
	return env->CallBooleanMethod(
			obj,
			env->GetMethodID(
				env->GetObjectClass(obj),
				name, signature
				)
			);
}

std::list<jobject> JNIInterface::CallListMethod(
		jobject obj, const char* name, const char* signature) const {
	jobject jlist = CallObjectMethod(
			obj, name, signature
			);

	std::list<jobject> cpp_list;
	jobject jiterator = CallObjectMethod(
			jlist, "iterator", "()" JITERATOR
			);

	while(CallBooleanMethod(jiterator, "hasNext", "()" JBOOLEAN)) {
		cpp_list.push_back(
				CallObjectMethod(
					jiterator, "next", "()Ljava/lang/Object;"
					));
	}

	return cpp_list;
}

chemmisol::Phase JNIInterface::CallPhaseMethod(
		jobject obj, const char* name, const char* signature) const {
	return map_phase.find(
			CallStringMethod(
				CallObjectMethod(obj, name, signature),
				"name", "()" JSTRING
			))->second;
}

