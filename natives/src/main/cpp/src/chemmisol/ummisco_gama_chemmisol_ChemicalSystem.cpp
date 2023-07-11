#include "chemmisol/ummisco_gama_chemmisol_ChemicalSystem.h"
#include "jni_interface.h"

using namespace chemmisol;

JNIEXPORT jlong JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_allocate
(JNIEnv *, jobject) {
	ChemicalSystem* system = new ChemicalSystem;
	return (jlong) system;
}

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_dispose
(JNIEnv *, jobject, jlong chemical_system_ptr) {
	delete (ChemicalSystem*) chemical_system_ptr;
}

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_addReaction
  (JNIEnv * env, jobject, jlong cpp_chemmical_system, jobject jreaction) {
	  JNIInterface interface(env);
	  std::string name = interface.CallStringMethod(
				  jreaction, "getName", "()" JSTRING
				  );

	  double logK = interface.CallDoubleMethod(jreaction,
					  "getLogK", "()" JDOUBLE
				  );

	  std::list<jobject> jreagents = interface.CallListMethod(jreaction,
			  "getReagents", "()Ljava/util/List;"
			  );

	  CHEM_JAVA_LOG(INFO) << "Adding reaction: " << name << " (log K=" << logK << "): ";
	  std::vector<ReactionComponent> reaction_components;
	  for(auto j : jreagents) {
		  reaction_components.emplace_back(
				  // name
				  interface.CallStringMethod(j,
					  "getName", "()" JSTRING
					  ),
				  // phase
				  interface.CallPhaseMethod(j,
					  "getPhase", "()" JPHASE
					  ),
				  // coefficient
				  interface.CallIntMethod(j,
					  "getCoefficient", "()" JINT
					  )
				  );
		  CHEM_JAVA_LOG(INFO) << "  " << reaction_components.back().coefficient << " "
			  << reaction_components.back().name
			  << " (" << reaction_components.back().phase << ")";
	  }
	  ((ChemicalSystem*) cpp_chemmical_system)->addReaction(
		  name, logK, reaction_components);
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_addComponent
  (JNIEnv * env, jobject, jlong cpp_chemical_system, jobject jcomponent) {
	  JNIInterface interface(env);
	  std::string name = 
		  interface.CallStringMethod(jcomponent, "getName", "()" JSTRING);
	  Phase phase =
		  interface.CallPhaseMethod(jcomponent, "getPhase", "()" JPHASE);
	  double concentration =
		  interface.CallDoubleMethod(jcomponent, "getConcentration", "()" JDOUBLE);

	  CHEM_JAVA_LOG(INFO) << "Adding component: " << name << " (" << phase << "): " << concentration;

	  ((ChemicalSystem*) cpp_chemical_system)->addComponent(
		  name, phase, concentration
		  );
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_fixPH
  (JNIEnv * env, jobject, jlong cpp_chemical_system, jdouble ph) {
	  ((ChemicalSystem*) cpp_chemical_system)->fixPH(ph);
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_solve
  (JNIEnv * env, jobject, jlong cpp_chemical_system) {
	  // TODO: 10 iterations for test purpose
	  ((ChemicalSystem*) cpp_chemical_system)->setMaxIteration(100);

	  ((ChemicalSystem*) cpp_chemical_system)->solveEquilibrium();
  }

JNIEXPORT jdouble JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_concentration
  (JNIEnv * env, jobject, jlong chemical_system_ptr, jstring jcomponent_name) {
	  JNIInterface interface(env);
	  return ((ChemicalSystem*) chemical_system_ptr)
		  ->getComponent(interface.convert(jcomponent_name))
			  .concentration();
  }
