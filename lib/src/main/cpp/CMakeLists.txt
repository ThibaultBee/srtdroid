cmake_minimum_required(VERSION 3.6)

include(ExternalProject)

set(OPENSSL_VERSION "1.1.1c")
set(SRT_VERSION "1.4.1")

find_program(GIT "git")
# Needed for openssl. Just to be sure you have it
find_program(MAKE "make")
find_program(PERL "perl")

set(CLANG_PATH ${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN}/bin)
if (CMAKE_HOST_WIN32)
    set(SCRIPT_EXT bat)
else ()
    set(SCRIPT_EXT sh)
endif ()
# Configure script to build openssl
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/build-openssl.${SCRIPT_EXT}.in ${CMAKE_CURRENT_BINARY_DIR}/build-openssl.${SCRIPT_EXT} @ONLY)

ExternalProject_Add(openssl_project
        URL https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz
        CONFIGURE_COMMAND ""
        BUILD_COMMAND ${CMAKE_CURRENT_BINARY_DIR}/build-openssl.${SCRIPT_EXT} <SOURCE_DIR>
        INSTALL_COMMAND ""
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a
        BUILD_IN_SOURCE 1
        )

add_library(ssl SHARED IMPORTED)
add_dependencies(ssl openssl_project)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a)

add_library(crypto SHARED IMPORTED)
add_dependencies(crypto openssl_project)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a)


# SRT
ExternalProject_Add(srt_project
        GIT_REPOSITORY https://github.com/Haivision/srt.git
        GIT_TAG v${SRT_VERSION}
        PATCH_COMMAND ${GIT} apply ${CMAKE_SOURCE_DIR}/patches/0001_srt_cmakefiles.patch
        CMAKE_ARGS -DUSE_OPENSSL_PC=OFF
        -DENABLE_APPS=OFF
        -DOPENSSL_INCLUDE_DIR=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include
        -DOPENSSL_CRYPTO_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.a
        -DOPENSSL_SSL_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.a
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_PREFIX=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_LIBDIR=.
        -DCMAKE_INSTALL_INCLUDEDIR=include
        -DCMAKE_INSTALL_BINDIR=bin
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DANDROID_TOOLCHAIN=${ANDROID_TOOLCHAIN}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_STL=${ANDROID_STL}
        -DANDROID_PIE=${ANDROID_PIE}
        -DANDROID_CPP_FEATURES=${ANDROID_CPP_FEATURES}
        -DANDROID_ALLOW_UNDEFINED_SYMBOLS=${ANDROID_ALLOW_UNDEFINED_SYMBOLS}
        -DANDROID_ARM_MODE=${ANDROID_ARM_MODE}
        -DANDROID_ARM_NEON=${ANDROID_ARM_NEON}
        -DANDROID_DISABLE_FORMAT_STRING_CHECKS=${ANDROID_DISABLE_FORMAT_STRING_CHECKS}
        -DANDROID_CCACHE=${ANDROID_CCACHE}
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libsrt.so
        DEPENDS crypto ssl
        BUILD_IN_SOURCE 1
        )

add_library(srt SHARED IMPORTED)
add_dependencies(srt srt_project)
set_target_properties(srt PROPERTIES IMPORTED_LOCATION ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libsrt.so)

# Target library
add_library(jnisrt SHARED glue.cpp enums.cpp structs.cpp)
include_directories(${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include)
target_link_libraries(jnisrt log android srt)
