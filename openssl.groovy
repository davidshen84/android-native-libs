pipeline {
  agent any
  environment {
    ANDROID_NDK = '/opt/android-ndk-r13b'
    ANDROID_API = 16
    LIB_NAME ='openssl-1.1.0c'
  }
  stages {
    stage('Download & unpack source') {
      steps {
        sh '[ -f ${LIB_NAME}.tar.gz ] || wget https://www.openssl.org/source/${LIB_NAME}.tar.gz'
        sh '[ -d ${LIB_NAME} ] || tar xfz ${LIB_NAME}.tar.gz'
      }
    }
    stage('Build arch=android, abi=armeabi') {
      environment {
        TOOLCHAIN_DIR = '/opt/${ARCH}-toolchain'
        SYSROOT = '${TOOLCHAIN_DIR}/sysroot'
        TOOL_PREFIX = '${TOOLCHAIN_DIR}/bin/${TOOL}'
        CC = '${TOOL_PREFIX}-gcc'
        LD = '${TOOL_PREFIX}-ld'
        AR = '${TOOL_PREFIX}-ar'
        RANLIB = '${TOOL_PREFIX}-ranlib'
        STRIP = '${TOOL_PREFIX}-strip'
        CFLAGS = '${ARCH_CFLAGS} -fpic -ffunction-sections -funwind-tables -fstack-protector -fno-strict-aliasing -finline-limit=64'
        LDFLAGS = '${ARCH_LDFLAGS}'
      }
      steps {
        script {
          def configs = [
            [arch: 'android', abi: 'armeabi', cflags: '-mthumb', ldflags: '', tool: 'arm-linux-androideabi', ndkArch: 'arm'],
            [arch: 'android-armeabi', abi: 'armeabi-v7a', cflags: '-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon', ldflags: '-march=armv7-a -Wl,--fix-cortex-a8', tool: 'arm-linux-androideabi', ndkArch: 'arm']
          ]

          for(c in configs) {
            withEnv(["ARCH=${c['arch']}",
                     "ABI=${c['abi']}",
                     "ARCH_CFLAGS=${c['cflags']}",
                     "ARCH_LDFLAGS=${c['ldflags']}",
                     "TOOL=${c['tool']}",
                     "NDK_ARCH=${c['ndkArch']}"]) {
              // create arm toolchain
              sh '[ -d /opt/${ARCH}-toolchain ] || python ${ANDROID_NDK}/build/tools/make_standalone_toolchain.py \
                        --arch=${NDK_ARCH} \
                        --api=${ANDROID_API} \
                        --stl=libc++ \
                        --install-dir=${TOOLCHAIN_DIR} \
                        --force'
              sh '([ -d build ] && rm -rf build) || mkdir build'
              // config && build
              dir('build') {
                sh '../${LIB_NAME}/Configure ${ARCH} \
                        --prefix=/opt/${LIB_NAME}-${ARCH}-output \
                        --sysroot=${SYSROOT} \
                        --with-zlib-include=${SYSROOT}/usr/include \
                        --with-zlib-lib=${SYSROOT}/usr/lib \
                        zlib \
                        no-asm no-shared no-unit-test'
                sh 'make -j5 && make install'
              }
            }
          }
        }
      }
    }
  }
}
