cmake_minimum_required(VERSION 3.24)

project(chemmisol-java)

set(CMAKE_CXX_STANDARD 14)

set(CMAKE_CXX_FLAGS_DEBUG "\
${CMAKE_CXX_FLAGS_DEBUG} -std=c++14 \
-Wall -Wextra -Wpedantic -Wno-error=pedantic \
-Woverloaded-virtual -Wnon-virtual-dtor \
-Wno-range-loop-construct \
")

include(FetchContent)
FetchContent_Declare(
  Chemmisol
  GIT_REPOSITORY https://github.com/u2worm/chemmisol-cpp
  GIT_TAG main
)

FetchContent_MakeAvailable(Chemmisol)

find_package(JNI REQUIRED OPTIONAL_COMPONENTS AWT JVM)

add_library(chemmisol-java
	src/jni_interface.cpp
	src/chemmisol/ummisco_gama_chemmisol_ChemicalSystem.cpp
	)

target_include_directories(chemmisol-java
	PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

target_link_libraries(chemmisol-java JNI::JNI chemmisol::chemmisol)

add_custom_command(TARGET chemmisol-java POST_BUILD
	COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:chemmisol::chemmisol>
	${CMAKE_CURRENT_BINARY_DIR}
)
