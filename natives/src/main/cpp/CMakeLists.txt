cmake_minimum_required(VERSION 3.24)

project(chemmisol-java)

set(CMAKE_CXX_STANDARD 14)

include(FetchContent)

FetchContent_Declare(
  chemmisol-cpp
  GIT_REPOSITORY https://github.com/u2worm/chemmisol-cpp
  GIT_TAG main
)

get_property(importTargets DIRECTORY "${CMAKE_SOURCE_DIR}" PROPERTY IMPORTED_TARGETS)

FetchContent_MakeAvailable(chemmisol-cpp)

find_package(JNI REQUIRED)

get_property(importTargetsAfter DIRECTORY "${CMAKE_SOURCE_DIR}" PROPERTY IMPORTED_TARGETS)
list(REMOVE_ITEM importTargetsAfter ${importTargets})

message("${importTargetsAfter}")

add_library(chemmisol-java
	src/chemmisol/ummisco_gama_chemmisol_ChemicalSystem.cpp

	)
target_include_directories(chemmisol-java
	PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

target_link_libraries(chemmisol-java JNI::JNI chemmisol)
