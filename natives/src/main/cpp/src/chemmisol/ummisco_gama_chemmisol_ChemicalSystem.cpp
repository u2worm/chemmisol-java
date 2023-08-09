#include "chemmisol/ummisco_gama_chemmisol_ChemicalSystem.h"
#include "jni_interface.h"

using namespace chemmisol;

JNIEXPORT jlong JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_allocate
(JNIEnv *, jclass) {
	ChemicalSystem* system = new ChemicalSystem;
	return (jlong) system;
}

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_dispose
(JNIEnv *, jclass, jlong chemical_system_ptr) {
	delete (ChemicalSystem*) chemical_system_ptr;
}

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_addReaction
  (JNIEnv * env, jclass, jlong cpp_chemmical_system, jobject jreaction) {
	  JNIInterface jni_interface(env);
	  std::string name = jni_interface.CallStringMethod(
				  jreaction, "getName", "()" JSTRING
				  );

	  double logK = jni_interface.CallDoubleMethod(jreaction,
					  "getLogK", "()" JDOUBLE
				  );

	  std::list<jobject> jreagents = jni_interface.CallListMethod(jreaction,
			  "getReagents", "()Ljava/util/List;"
			  );

	  CHEM_JAVA_LOG(INFO) << "Adding reaction: " << name << " (log K=" << logK << "): ";
	  std::vector<Reagent> reaction_components;
	  for(auto j : jreagents) {
		  reaction_components.emplace_back(
				  // name
				  jni_interface.CallStringMethod(j,
					  "getName", "()" JSTRING
					  ),
				  // phase
				  jni_interface.CallPhaseMethod(j,
					  "getPhase", "()" JPHASE
					  ),
				  // coefficient
				  jni_interface.CallIntMethod(j,
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
  (JNIEnv * env, jclass, jlong cpp_chemical_system, jobject jcomponent) {
	  JNIInterface jni_interface(env);
	  std::string name = 
		  jni_interface.CallStringMethod(jcomponent, "getName", "()" JSTRING);
	  Phase phase =
		  jni_interface.CallPhaseMethod(jcomponent, "getPhase", "()" JPHASE);
	  double concentration =
		  jni_interface.CallDoubleMethod(jcomponent, "getConcentration", "()" JDOUBLE);

	  CHEM_JAVA_LOG(INFO) << "Adding component: " << name << " (" << phase << "): " << concentration;

	  ((ChemicalSystem*) cpp_chemical_system)->addComponent(
		  name, phase, concentration
		  );
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_fixPH
  (JNIEnv *, jclass, jlong cpp_chemical_system, jdouble ph) {
	  CHEM_JAVA_LOG(INFO) << "Fixing pH to " << ph << ".";
	  ((ChemicalSystem*) cpp_chemical_system)->fixPH(ph);
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_solve
  (JNIEnv * env, jclass, jlong cpp_chemical_system) {
	  JNIInterface jni_interface(env);
	  CHEM_JAVA_LOG(INFO) << "Solving system using 100 iterations.";
	  ((ChemicalSystem*) cpp_chemical_system)->setMaxIteration(100);
	  try {
		  ((ChemicalSystem*) cpp_chemical_system)->solveEquilibrium();
	  }
	  catch (const MissingProducedSpeciesInReaction& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::MissingProducedSpeciesInReaction",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
	  catch (const InvalidSpeciesInReaction& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::InvalidSpeciesInReaction",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
	  CHEM_JAVA_LOG(INFO) << "Done.";
  }

JNIEXPORT jdouble JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_concentration
  (JNIEnv * env, jclass, jlong chemical_system_ptr, jstring jspecies_name) {
	  JNIInterface jni_interface(env);
	  return ((ChemicalSystem*) chemical_system_ptr)
		  ->getSpecies(jni_interface.convert(jspecies_name))
			  .concentration();
  }
