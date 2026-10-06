# CMakeLists.txt for AdrenoBox Low-Level Core
cmake_minimum_required(VERSION 3.22.1)
project("adrenobox_core")

# Compilation flags optimized for ARMv8-A / ARMv9 (Snapdragon 8 Gen 1/2/3/4)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -fPIC -D_GNU_SOURCE -funroll-loops -march=armv8-a+crypto+crc -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fPIC -D_GNU_SOURCE -funroll-loops -std=c++17 -march=armv8-a+crypto+crc")

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Build native bridge shared library
add_library(
    adrenobox_core
    SHARED
    adrenobox_native.c
)

# Link against Android NDK libraries:
# - log: __android_log_print
# - android: ANativeWindow, ASharedMemory, APerformanceHint
# - dl: dlopen, dlsym for dynamic loading of Turnip (libvulkan_freedreno.so)
find_library(log-lib log)
find_library(android-lib android)
find_library(dl-lib dl)

target_link_libraries(
    adrenobox_core
    ${log-lib}
    ${android-lib}
    ${dl-lib}
)
