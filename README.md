# README

- Jenkinsfile, Jenkins pipeline to build the following libraries:
  - OpenSSL
  - libcurl
  - protobuf
- Mount host **Android NDK** at `/var/android-ndk`

## Jenkins parameters

When defining a Jenkins pipeline, the following paramiters **must** be
defined to control the pipeline behavior.

- ANDROID_API (*string*)

  The Android API level.

- ABI_FILTER (*string*)

  Space sparated ABI values. A subset of these values:

      armeabi armeabi-v7a x86 mips arm64-v8a x86_64 mips64

- BUILD_OPENSSL (*boolean*)

  Enable or disable building `OpenSSL` library.

- BUILD_LIBCURL (*boolean*)

  Enable or disable building `libcurl` library.

- BUILD_PROTOBUF (*boolean*)

  Enable or disable building `protobuf` library.
