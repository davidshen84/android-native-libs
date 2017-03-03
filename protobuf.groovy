pipeline {
  agent any
  environment {
    ANDROID_NDK = '/var/android-ndk'
    PROTOBUF_ARCHIVE_VERSION = '3.1.0'
    PROTOBUF_ARCHIVE_NAME ='protobuf-${PROTOBUF_ARCHIVE_VERSION}'
  }
  stages {
    stage('Download & unpack protobuf source') {
      steps {
        script {
          if (!fileExists("${env['PROTOBUF_ARCHIVE_NAME']}-env['PROTOBUF_ARCHIVE_VERSION']")) {
            sh 'curl https://codeload.github.com/google/${PROTOBUF_ARCHIVE_NAME%%-*}/tar.gz/v${PROTOBUF_ARCHIVE_VERSION} | tar xz'
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

          def protobuf_archive_name = "${env['PROTOBUF_ARCHIVE_NAME']}"
          echo "${protobuf_archive_name}"
          for(c in configs) {
            def toolchain_dir = "${env['JENKINS_HOME']}/android-${ANDROID_API}-${c['abi']}-toolchain"
            def tool_prefix = "${toolchain_dir}/bin/${c['tool']}"

            withEnv(["ARCH=${c['arch']}",
                     "ABI=${c['abi']}",
                     "ANDROID_API=${ANDROID_API}",
                     "ARCH_CFLAGS=${c['cflags']}",
                     "ARCH_LDFLAGS=${c['ldflags']}",
                     "LIBS=-lc++_static -latomic",
                     "NDK_ARCH=${c['ndkArch']}",
                     "TOOLCHAIN_DIR=${toolchain_dir}",
                     "SYSROOT=${toolchain_dir}/sysroot",
                     // tool path
                     // "CC=${toolchain_dir}/bin/clang",
                     "CXX=${toolchain_dir}/bin/clang++",
                     "LD=${tool_prefix}-ld",
                     "AR=${tool_prefix}-ar",
                     "RANLIB=${tool_prefix}-ranlib",
                     "STRIP=${tool_prefix}-strip",
                     // flags
                     // "CFLAGS=${c['cflags']} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing -finline-limit=64",
                     "CXXFLAGS=${c['cflags']} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing",
                     "LDFLAGS=${c['ldflags']} -static-libstdc++"]) {
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
              // config && build
              dir('build') {
                sh '../${protobuf_archive_name}/autogen.sh'
                sh '../${protobuf_archive_name}/configure \
                    --prefix=/opt/output/${PROTOBUF_ARCHIVE_NAME}-android-${ANDROID_API}-${ABI} \
                    --with-sysroot=${SYSROOT}                           \
                    --with-protoc=`which protoc`                        \
                    --with-zlib                                         \
                    --host=${TOOL}                                      \
                    --enable-static                                     \
                    --disable-shared                                    \
                    --enable-cross-compile'
                sh 'make -j5 && make install'
              }
            }
          }
        }
      }
    }
  }
}
