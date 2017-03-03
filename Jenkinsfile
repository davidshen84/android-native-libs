pipeline {
  agent any
  environment {
    ANDROID_NDK = '/var/android-ndk'
    MAKE_OPTS = '-j3'
    OPENSSL_ARCHIVE_NAME ='openssl-1.1.0c'
    CURL_ARCHIVE_NAME = 'curl-7.51.0'
    PROTOBUF_ARCHIVE_NAME ='protobuf-3.1.0'
  }
  stages {
    stage('Download & unpack OpenSSL source') {
      steps {
        script {
          if (!fileExists("${env['OPENSSL_ARCHIVE_NAME']}")) {
            sh 'curl https://www.openssl.org/source/${OPENSSL_ARCHIVE_NAME}.tar.gz | tar xz'
          }
        }
      }
    }
    stage('Build OpenSSL') {
      steps {
        script {
          def configs = [
            [arch: 'android',
             abi: 'armeabi',
             cflags: '-mthumb',
             ldflags: '',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-armeabi',
             abi: 'armeabi-v7a',
             cflags: '-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon',
             ldflags: '-Wl,--fix-cortex-a8',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-x86',
             abi: 'x86',
             cflags: '-march=i686 -mtune=intel -msse3 -mfpmath=sse -m32',
             ldflags: '',
             ndkArch: 'x86',
             tool: 'i686-linux-android'],
            [arch: 'android-mips',
             abi: 'mips',
             cflags: '',
             ldflags: '',
             ndkArch: 'mips',
             tool: 'mipsel-linux-android']
            ]

          // Only build 64bit arch if ANDROID_API is above level 20
          if ("${ANDROID_API}".toInteger() > 20) {
            configs << [arch: 'android64-aarch64',
                        abi: 'arm64-v8a',
                        cflags: '',
                        ldflags: '',
                        ndkArch: 'arm64',
                        tool: 'aarch64-linux-android']

            configs << [arch: 'android64',
                        abi: 'x86_64',
                        cflags: '-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel',
                        ldflags: '',
                        ndkArch: 'x86_64',
                        tool: 'x86_64-linux-android']

            configs << [arch: 'linux64-mips64',
                        abi: 'mips',
                        cflags: '',
                        ldflags: '',
                        ndkArch: 'mips64',
                        tool: 'mips64el-linux-android']
          }

          for (c in configs) {
            def toolchain_dir = "${env['JENKINS_HOME']}/android-${ANDROID_API}-${c['abi']}-toolchain"
            def tool_prefix = "${toolchain_dir}/bin/${c['tool']}"

            withEnv(["ARCH=${c['arch']}",
                     "ABI=${c['abi']}",
                     "ANDROID_API=${ANDROID_API}",
                     "ARCH_CFLAGS=${c['cflags']}",
                     "ARCH_LDFLAGS=${c['ldflags']}",
                     "NDK_ARCH=${c['ndkArch']}",
                     "TOOLCHAIN_DIR=${toolchain_dir}",
                     "SYSROOT=${toolchain_dir}/sysroot",
                     // tool path
                     "CC=${tool_prefix}-gcc",
                     "LD=${tool_prefix}-ld",
                     "AR=${tool_prefix}-ar",
                     "RANLIB=${tool_prefix}-ranlib",
                     "STRIP=${tool_prefix}-strip",
                     // flags
                     "CFLAGS=${c['cflags']} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing -finline-limit=64",
                     "LDFLAGS=${c['ldflags']}"]) {
              // create toolchain
              sh '[ -d ${TOOLCHAIN_DIR} ] || python ${ANDROID_NDK}/build/tools/make_standalone_toolchain.py \
                        --arch=${NDK_ARCH} \
                        --api=${ANDROID_API} \
                        --stl=libc++ \
                        --install-dir=${TOOLCHAIN_DIR}'

              if(fileExists('build')) {
                sh 'rm -rf build'
              }
              sh 'mkdir build'

              // config && make
              dir('build') {
                sh '../${OPENSSL_ARCHIVE_NAME}/Configure ${ARCH} \
                        --prefix=/opt/output/${OPENSSL_ARCHIVE_NAME}-android-${ANDROID_API}-${ABI} \
                        --sysroot=${SYSROOT} \
                        --with-zlib-include=${SYSROOT}/usr/include \
                        --with-zlib-lib=${SYSROOT}/usr/lib \
                        zlib \
                        no-asm no-shared no-unit-test'
                sh 'make ${MAKE_OPTS} && make install'
              }
            }
          }
        }
      }
    }

    stage('Download & unpack libcurl source') {
      steps {
        script {
          if (!fileExists("${env['CURL_ARCHIVE_NAME']}")) {
            sh 'curl https://curl.haxx.se/download/${CURL_ARCHIVE_NAME}.tar.gz | tar xz'
          }
        }
      }
    }
    stage('Build curl') {
      steps {
        script {
          def configs = [
            [arch: 'android',
             abi: 'armeabi',
             cflags: '-mthumb',
             ldflags: '',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-armeabi',
             abi: 'armeabi-v7a',
             cflags: '-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon',
             ldflags: '-Wl,--fix-cortex-a8',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-x86',
             abi: 'x86',
             cflags: '-march=i686 -mtune=intel -msse3 -mfpmath=sse -m32',
             ldflags: '',
             ndkArch: 'x86',
             tool: 'i686-linux-android']
            ]

          // Only build 64bit arch if ANDROID_API is above level 20
          if ("${ANDROID_API}".toInteger() > 20) {
            configs << [arch: 'android64-aarch64',
                        abi: 'arm64-v8a',
                        cflags: '',
                        ldflags: '',
                        ndkArch: 'arm64',
                        tool: 'aarch64-linux-android']

            configs << [arch: 'android64',
                        abi: 'x86_64',
                        cflags: '-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel',
                        ldflags: '',
                        ndkArch: 'x86_64',
                        tool: 'x86_64-linux-android']

            configs << [arch: 'android-mips',
                        abi: 'mips',
                        cflags: '',
                        ldflags: '',
                        ndkArch: 'mips',
                        tool: 'mipsel-linux-android']

            configs << [arch: 'linux64-mips64',
                        abi: 'mips',
                        cflags: '',
                        ldflags: '',
                        ndkArch: 'mips64',
                        tool: 'mips64el-linux-android']
          }

          for(c in configs) {
            def toolchain_dir = "${env['JENKINS_HOME']}/android-${ANDROID_API}-${c['abi']}-toolchain"
            def tool_prefix = "${toolchain_dir}/bin/${c['tool']}"
            def openssl_root = "/opt/output/${env['OPENSSL_ARCHIVE_NAME']}-android-${ANDROID_API}-${c['abi']}"

            if(!fileExists(openssl_root)) {
              error "can not find ${openssl_root}."
            }

            withEnv(["ARCH=${c['arch']}",
                     "ABI=${c['abi']}",
                     "ANDROID_API=${ANDROID_API}",
                     "ARCH_CFLAGS=${c['cflags']}",
                     "ARCH_LDFLAGS=${c['ldflags']}",
                     "NDK_ARCH=${c['ndkArch']}",
                     "TOOL=${c['tool']}",
                     "TOOLCHAIN_DIR=${toolchain_dir}",
                     "SYSROOT=${toolchain_dir}/sysroot",
                     "OPENSSL_ROOT=${openssl_root}",
                     // tool path
                     "CC=${tool_prefix}-gcc",
                     "LD=${tool_prefix}-ld",
                     "AR=${tool_prefix}-ar",
                     "RANLIB=${tool_prefix}-ranlib",
                     "STRIP=${tool_prefix}-strip",
                     // flags
                     "CFLAGS=${c['cflags']} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing -finline-limit=64",
                     "LDFLAGS=${c['ldflags']}"]) {

              // create arm toolchain
              sh '[ -d ${TOOLCHAIN_DIR} ] || python ${ANDROID_NDK}/build/tools/make_standalone_toolchain.py \
                        --arch=${NDK_ARCH}                              \
                        --api=${ANDROID_API}                            \
                        --stl=libc++                                    \
                        --install-dir=${TOOLCHAIN_DIR}'

              if (fileExists('build')) {
                sh 'rm -rf build'
              }
              sh 'mkdir build'

              // config && make
              dir('build') {
                sh '../${CURL_ARCHIVE_NAME}/configure \
                      --prefix=/opt/output/${CURL_ARCHIVE_NAME}-android-${ANDROID_API}-${ABI} \
                      --with-sysroot=${SYSROOT}                         \
                      --host=${TOOL}                                    \
                      --with-ssl=${OPENSSL_ROOT}                        \
                      --enable-ipv6 --enable-static                     \
                      --enable-threaded-resolver                        \
                      --disable-shared                                  \
                      --disable-dict --disable-gopher                   \
                      --disable-ldap --disable-ldaps                    \
                      --disable-pop3 --disable-smtp --disable-imap      \
                      --disable-smb --disable-telnet --disable-rtsp     \
                      --disable-manual --disable-verbose'
                sh 'make ${MAKE_OPTS} && make install'
              }
            }
          }
        }
      }
    }

    stage('Download & unpack protobuf source') {
      steps {
        script {
          if (!fileExists("${env['PROTOBUF_ARCHIVE_NAME']}")) {
            sh 'curl https://codeload.github.com/google/${PROTOBUF_ARCHIVE_NAME%%-*}/tar.gz/v${PROTOBUF_ARCHIVE_NAME#*-} | tar xz'

            dir("${PROTOBUF_ARCHIVE_NAME}") {
              sh './autogen.sh'
            }
          }
        }
      }
    }
    stage('Build protobuf') {
      steps {
        script {
          def configs = [
            [arch: 'android',
             abi: 'armeabi',
             cflags: '-mthumb',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-armeabi',
             abi: 'armeabi-v7a',
             cflags: '-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon',
             ldflags: '-Wl,--fix-cortex-a8',
             ndkArch: 'arm',
             tool: 'arm-linux-androideabi'],
            [arch: 'android-x86',
             abi: 'x86',
             cflags: '-march=i686 -mtune=intel -msse3 -mfpmath=sse -m32',
             ndkArch: 'x86',
             tool: 'i686-linux-android']
            ]

          // Only build 64bit arch if ANDROID_API is above level 20
          if ("${ANDROID_API}".toInteger() > 20) {
            configs << [arch: 'android64-aarch64',
                        abi: 'arm64-v8a',
                        ndkArch: 'arm64',
                        tool: 'aarch64-linux-android']

            configs << [arch: 'android64',
                        abi: 'x86_64',
                        cflags: '-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel',
                        ndkArch: 'x86_64',
                        tool: 'x86_64-linux-android']

            configs << [arch: 'android-mips',
                        abi: 'mips',
                        ndkArch: 'mips',
                        tool: 'mipsel-linux-android']

            configs << [arch: 'linux64-mips64',
                        abi: 'mips',
                        ndkArch: 'mips64',
                        tool: 'mips64el-linux-android']
          }

          for(c in configs) {
            def toolchain_dir = "${env['JENKINS_HOME']}/android-${ANDROID_API}-${c['abi']}-toolchain"
            def tool_prefix = "${toolchain_dir}/bin/${c['tool']}"

            withEnv(["ARCH=${c['arch']}",
                     "ABI=${c['abi']}",
                     "ANDROID_API=${ANDROID_API}",
                     "LIBS=-lc++_static -latomic",
                     "NDK_ARCH=${c['ndkArch']}",
                     "TOOLCHAIN_DIR=${toolchain_dir}",
                     "TOOL=${c['tool']}",
                     "SYSROOT=${toolchain_dir}/sysroot",
                     // tool path
                     "CC=${toolchain_dir}/bin/clang",
                     "CXX=${toolchain_dir}/bin/clang++",
                     "LD=${tool_prefix}-ld",
                     "AR=${tool_prefix}-ar",
                     "RANLIB=${tool_prefix}-ranlib",
                     "STRIP=${tool_prefix}-strip",
                     // flags
                     "CFLAGS=${c['cflags'] ?: ''} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing",
                     "CXXFLAGS=${c['cflags'] ?: ''} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing -std=c++11 -frtti -fexceptions",
                     "LDFLAGS=${c['ldflags'] ?: ''}"]) {

              // create toolchain
              sh '[ -d ${TOOLCHAIN_DIR} ] || python ${ANDROID_NDK}/build/tools/make_standalone_toolchain.py \
                        --arch=${NDK_ARCH} \
                        --api=${ANDROID_API} \
                        --stl=libc++ \
                        --install-dir=${TOOLCHAIN_DIR}'

              if (fileExists('build')) {
                sh 'rm -rf build'
              }
              sh 'mkdir build'

              // config && make
              dir('build') {
                sh '../${PROTOBUF_ARCHIVE_NAME}/configure \
                    --prefix=/opt/output/${PROTOBUF_ARCHIVE_NAME}-android-${ANDROID_API}-${ABI} \
                    --with-sysroot=${SYSROOT}                           \
                    --with-protoc=`which protoc`                        \
                    --with-zlib                                         \
                    --host=${TOOL}                                      \
                    --enable-static                                     \
                    --enable-cross-compile                              \
                    --disable-shared'
                sh 'make ${MAKE_OPTS} && make install'
              }
            }
          }
        }
      }
    }
  }
}
