cmake_minimum_required(VERSION 3.22.1)

add_library(libmp3lame SHARED IMPORTED)
add_library(libmpg123 SHARED IMPORTED)

set_target_properties(libmp3lame PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI}/libmp3lame.so)

set_target_properties(libmpg123 PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI}/libmpg123.so)

add_library(liblame-jni SHARED lame-jni.cpp)
add_library(libmpg-jni SHARED mpg-jni.cpp)

include_directories(${PROJECT_SOURCE_DIR})

target_link_libraries(liblame-jni
        libmp3lame
        ${log-lib}
        log
)
target_link_libraries(libmpg-jni
        libmpg123
        ${log-lib}
        log
)