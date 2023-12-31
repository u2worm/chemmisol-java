#include "chemmisol/ummisco_gama_chemmisol_ChemicalSystem.h"
#include "jni_interface.h"

using namespace chemmisol;

JNIEXPORT jlong JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_allocate__
  (JNIEnv *, jclass) {
	ChemicalSystem* system = new ChemicalSystem();
	return (jlong) system;
}

JNIEXPORT jlong JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_allocate__DDD
(JNIEnv * env, jclass,
 jdouble solid_concentration,
 jdouble specific_surface_area,
 jdouble site_concentration) {
	JNIInterface jni_interface(env);
	ChemicalSystem* system = new ChemicalSystem(
			solid_concentration,
			specific_surface_area,
			site_concentration);
	CHEM_JAVA_LOG(INFO) << "Mineral system: " << system->sitesQuantity();
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
		  jni_interface.CallDoubleMethod(jcomponent, "getTotalConcentration", "()" JDOUBLE);

	  CHEM_JAVA_LOG(INFO) << "Adding component: " << name << " (" << phase << "): " << concentration;

	  try {
	  ((ChemicalSystem*) cpp_chemical_system)->addComponent(
		  name, phase, concentration
		  );
	  } catch(const InvalidMineralSpeciesWithUndefinedSitesCount& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::InvalidMineralSpeciesWithUndefinedSitesCount",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_fixPH
  (JNIEnv * env, jclass, jlong cpp_chemical_system, jdouble ph, jstring h_component_name) {
	  JNIInterface jni_interface(env);
	  std::string _component_name = jni_interface.convert(h_component_name);
	  CHEM_JAVA_LOG(INFO) << "Fixing pH to " << ph << " in the " << _component_name << " component.";
	  ((ChemicalSystem*) cpp_chemical_system)->fixPH(ph, _component_name);
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_setTotalConcentration
  (JNIEnv * env, jclass, jlong cpp_chemical_system, jstring jcomponent_name, jdouble concentration) {
	  JNIInterface jni_interface(env);
	  std::string _component_name = jni_interface.convert(jcomponent_name);
	  CHEM_JAVA_LOG(INFO) << "Set total concentration of " << _component_name << " to " << concentration << ".";
	  ((ChemicalSystem*) cpp_chemical_system)->setTotalConcentration(
		  ((ChemicalSystem*) cpp_chemical_system)->getComponent(_component_name),
		  concentration
		  );
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_setUp
  (JNIEnv * env, jclass, jlong cpp_chemical_system) {
	  JNIInterface jni_interface(env);
	  try {
		  ((ChemicalSystem*) cpp_chemical_system)->setUp();
	  }
	  catch (const MissingProducedSpeciesInReaction& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::MissingProducedSpeciesInReaction",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
	  catch (const TooManyProducedSpeciesInReaction& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::InvalidSpeciesInReaction",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
  }

JNIEXPORT void JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_solve
  (JNIEnv * env, jclass, jlong cpp_chemical_system) {
	  JNIInterface jni_interface(env);
	  CHEM_JAVA_LOG(INFO) << "Solving system using 1000 iterations.";
	  ((ChemicalSystem*) cpp_chemical_system)->setMaxIteration(1000);
	  try {
		  ((ChemicalSystem*) cpp_chemical_system)->solveEquilibrium();
	  }
	  catch (const MissingProducedSpeciesInReaction& e) {
		  jni_interface.ThrowNew(
				  "chemmisol::MissingProducedSpeciesInReaction",
				  "ummisco/gama/chemmisol/ChemicalSystem$ChemmisolCoreException",
				  e);
	  }
	  catch (const TooManyProducedSpeciesInReaction& e) {
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

JNIEXPORT jdouble JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_reactionQuotient
  (JNIEnv * env, jclass, jlong chemical_system_ptr, jstring jreaction_name) {
	  JNIInterface jni_interface(env);
	  return ((ChemicalSystem*) chemical_system_ptr)
		  ->reactionQuotient(jni_interface.convert(jreaction_name));
  }

JNIEXPORT jdouble JNICALL Java_ummisco_gama_chemmisol_ChemicalSystem_sitesQuantity
  (JNIEnv *, jclass, jlong chemical_system_ptr) {
	  return ((ChemicalSystem*) chemical_system_ptr)
		  ->sitesQuantity();
  }
