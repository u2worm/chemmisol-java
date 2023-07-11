cmake_minimum_required(VERSION 3.24)

project(chemmisol-java)

set(CMAKE_CXX_STANDARD 14)

include(FetchContent)
FetchContent_Declare(
  Chemmisol
  GIT_REPOSITORY https://github.com/u2worm/chemmisol-cpp
  GIT_TAG main
)

FetchContent_MakeAvailable(Chemmisol)

if(${Chemmisol_FOUND})
	# find_package() IMPORTED target, that follows the <namespace>::<target>
	# naming convention
	set(CHEMMISOL_TARGET chemmisol::chemmisol)
else()
	# Locally built target from FetchContent, just named <target> since its not
	# imported
	set(CHEMMISOL_TARGET chemmisol)
endif()

find_package(JNI REQUIRED OPTIONAL_COMPONENTS AWT JVM)

add_library(chemmisol-java
	src/jni_interface.cpp
	src/chemmisol/ummisco_gama_chemmisol_ChemicalSystem.cpp
	)

target_include_directories(chemmisol-java
	PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

target_link_libraries(chemmisol-java JNI::JNI ${CHEMMISOL_TARGET})

add_custom_command(TARGET chemmisol-java POST_BUILD
	COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:${CHEMMISOL_TARGET}>
	${CMAKE_CURRENT_BINARY_DIR}
)
