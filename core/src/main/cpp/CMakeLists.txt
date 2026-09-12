cmake_minimum_required(VERSION 3.0)

# Source archives do not include Git metadata. Query the kernel checkout directly
# when available, and keep version generation valid for archive-based builds.
set(COMMIT_HASH "unknown")
set(CURRENT_BRANCH "unknown")
set(KERNEL_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../foss/golang/clash")
find_package(Git QUIET)

if(GIT_FOUND AND EXISTS "${KERNEL_SOURCE_DIR}/.git")
  execute_process(
    COMMAND "${GIT_EXECUTABLE}" rev-parse --short=7 HEAD
    WORKING_DIRECTORY "${KERNEL_SOURCE_DIR}"
    RESULT_VARIABLE GIT_HASH_RESULT
    OUTPUT_VARIABLE GIT_HASH_OUTPUT
    OUTPUT_STRIP_TRAILING_WHITESPACE
    ERROR_QUIET
  )
  if(GIT_HASH_RESULT EQUAL 0 AND GIT_HASH_OUTPUT MATCHES "^[0-9a-fA-F]+$")
    set(COMMIT_HASH "${GIT_HASH_OUTPUT}")
    execute_process(
      COMMAND "${GIT_EXECUTABLE}" branch -r --contains HEAD "--format=%(refname:short)"
      WORKING_DIRECTORY "${KERNEL_SOURCE_DIR}"
      RESULT_VARIABLE GIT_BRANCH_RESULT
      OUTPUT_VARIABLE GIT_BRANCH_OUTPUT
      OUTPUT_STRIP_TRAILING_WHITESPACE
      ERROR_QUIET
    )
    if(GIT_BRANCH_RESULT EQUAL 0 AND NOT GIT_BRANCH_OUTPUT STREQUAL "")
      string(REPLACE "\n" ";" GIT_BRANCHES "${GIT_BRANCH_OUTPUT}")
      list(GET GIT_BRANCHES 0 CURRENT_BRANCH)
      string(REGEX REPLACE "^origin/" "" CURRENT_BRANCH "${CURRENT_BRANCH}")
      string(STRIP "${CURRENT_BRANCH}" CURRENT_BRANCH)
    endif()
  endif()
endif()

message(STATUS "git hash= ${COMMIT_HASH}")
message(STATUS "git current branch = ${CURRENT_BRANCH}")

# 获取生成时间
string(TIMESTAMP COMPILE_TIME "%y%m%d")
string (REGEX REPLACE "[\n\t\r]" "" COMPILE_TIME ${COMPILE_TIME})
string(REGEX REPLACE "\"" "" COMPILE_TIME ${COMPILE_TIME})

# 生成版本信息
set(GIT_VERSION "${CURRENT_BRANCH}_${COMMIT_HASH}_${COMPILE_TIME}")
message(STATUS "version info = ${GIT_VERSION}")

# 去除空格
string(REGEX REPLACE "[ ]+" "" GIT_VERSION "${GIT_VERSION}")

# 保存变量到文件
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/version.h.in ${CMAKE_CURRENT_SOURCE_DIR}/version.h @ONLY)

project(clash-bridge C)


set(CMAKE_POSITION_INDEPENDENT_CODE on)
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O3")

set(GO_OUTPUT_BASE ${GO_OUTPUT}/${FLAVOR_NAME})

if ("${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Debug")
elseif ("${CMAKE_BUILD_TYPE}" STREQUAL "Release")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Release")
elseif ("${CMAKE_BUILD_TYPE}" STREQUAL "RelWithDebInfo")
    set(GO_OUTPUT_BASE "${GO_OUTPUT_BASE}Release")
else ()
    message(FATAL_ERROR "Unknown build type ${CMAKE_BUILD_TYPE}")
endif ()

include_directories("${GO_OUTPUT_BASE}/${CMAKE_ANDROID_ARCH_ABI}")
include_directories("${GO_SOURCE}")

link_directories("${GO_OUTPUT_BASE}/${CMAKE_ANDROID_ARCH_ABI}")

add_library(bridge SHARED main.c jni_helper.c bridge_helper.c)
target_link_libraries(bridge log clash)
