#include <cstring>
#include <jni.h>
#include <syslog.h>
#include <nunchuk.h>
#include "provider.h"
#include "serializer.h"
#include "deserializer.h"

using namespace nunchuk;

extern "C"
JNIEXPORT jobject

JNICALL
Java_com_nunchuk_android_nativelib_LibNunchukAndroid_getWallets(JNIEnv *env, jobject thiz) {
    syslog(LOG_DEBUG, "[JNI]getWallets()");
    auto wallets = NunchukProvider::get()->nu->GetWallets();
    syslog(LOG_DEBUG, "[JNI]wallets::%lu", wallets.size());
    return Deserializer::convert2JWallets(env, wallets);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_nunchuk_android_nativelib_LibNunchukAndroid_createWallet(
        JNIEnv *env,
        jobject thiz,
        jstring name,
        jint total_require_signs,
        jobject signers,
        jint address_type,
        jboolean is_escrow,
        jstring description
) {

    const std::vector<SingleSigner> &singleSigners = Serializer::convert2CSigners(env, signers);
    AddressType type = Serializer::convert2CAddressType(address_type);
    const Wallet &wallet = NunchukProvider::get()->nu->CreateWallet(
            env->GetStringUTFChars(name, nullptr),
            singleSigners.size(),
            total_require_signs,
            singleSigners,
            type,
            is_escrow,
            env->GetStringUTFChars(description, nullptr)
    );
    syslog(LOG_DEBUG, "[JNI][wallet]name::%s", wallet.get_name().c_str());
    syslog(LOG_DEBUG, "[JNI][wallet]address_type::%d", wallet.get_address_type());
    syslog(LOG_DEBUG, "[JNI][wallet]signers::%lu", wallet.get_signers().size());
    return Deserializer::convert2JWallet(env, wallet);
}