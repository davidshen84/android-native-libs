# README

- Jenkinsfile, Jenkins pipeline to build the following libraries:
  - OpenSSL
  - libcurl
  - protobuf
- Mount host **Android NDK** at `/var/android-ndk`

## Jenkins parameters

When defining a Jenkins pipeline, the following paramiters must be
defined to control the pipeline behavior.

- ANDROID_API (*string*, **required**)

  The Android API level.
- ABI_FILTER (*string*, **required**)

  Space sparated ABI values.

- BUILD_OPENSSL (*boolean*, **required**)

  Enable or disable building `OpenSSL` library.

- BUILD_LIBCURL (*boolean*, **required**)

  Enable or disable building `libcurl` library.

- BUILD_PROTOBUF (*boolean*, **required**)

  Enable or disable building `protobuf` library.
