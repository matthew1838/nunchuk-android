#include <cstring>
#include <jni.h>
#include <syslog.h>
#include <nunchuk.h>
#include "deserializer.h"

using namespace nunchuk;

jobject Deserializer::convert2JAddressType(JNIEnv *env, const AddressType &type) {
    jclass clazz = env->FindClass("com/nunchuk/android/type/AddressTypeHelper");
    jmethodID staticMethod = env->GetStaticMethodID(clazz, "from", "(I)Lcom/nunchuk/android/type/AddressType;");
    return env->CallStaticObjectMethod(clazz, staticMethod, (int) type);
}

jobject Deserializer::convert2JSigner(JNIEnv *env, const SingleSigner &signer) {
    syslog(LOG_DEBUG, "[JNI] convert2JSigner()");
    jclass clazz = env->FindClass("com/nunchuk/android/model/SingleSigner");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");
    jobject instance = env->NewObject(clazz, constructor);
    try {
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setName", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_name().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setXpub", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_xpub().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setDerivationPath", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_derivation_path().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setMasterFingerprint", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_master_fingerprint().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setPublicKey", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_public_key().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setMasterSignerId", "(Ljava/lang/String;)V"), env->NewStringUTF(signer.get_master_signer_id().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setLastHealthCheck", "(J)V"), signer.get_last_health_check());
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setUsed", "(Z)V"), signer.is_used());
    } catch (const std::exception &e) {
        Deserializer::convert2JException(env, e.what());
        syslog(LOG_DEBUG, "[JNI] convert2JSigner error::%s", e.what());
    }
    return instance;
}

jobject Deserializer::convert2JSigners(JNIEnv *env, const std::vector<SingleSigner> &signers) {
    syslog(LOG_DEBUG, "[JNI] convert2JSigners()");
    static auto arrayListClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    static jmethodID constructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    jobject arrayListInstance = env->NewObject(arrayListClass, constructor);
    for (const SingleSigner &s: signers) {
        jobject element = convert2JSigner(env, s);
        env->CallBooleanMethod(arrayListInstance, addMethod, element);
        env->DeleteLocalRef(element);
    }
    return arrayListInstance;
}

jobject Deserializer::convert2JWallet(JNIEnv *env, const Wallet &wallet) {
    jclass clazz = env->FindClass("com/nunchuk/android/model/Wallet");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");
    jobject instance = env->NewObject(clazz, constructor);
    try {
        jobject signers = Deserializer::convert2JSigners(env, wallet.get_signers());
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setId", "(Ljava/lang/String;)V"), env->NewStringUTF(wallet.get_id().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setName", "(Ljava/lang/String;)V"), env->NewStringUTF(wallet.get_name().c_str()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setTotalRequireSigns", "(I)V"), wallet.get_n());
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setSigners", "(Ljava/util/List;)V"), signers);
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setEscrow", "(Z)V"), wallet.is_escrow());
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setBalance", "(D)V"), 0.0);
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setAddressType", "(Lcom/nunchuk/android/type/AddressType;)V"), Deserializer::convert2JAddressType(env, wallet.get_address_type()));
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setCreateDate", "(J)V"), wallet.get_create_date());
        env->CallVoidMethod(instance, env->GetMethodID(clazz, "setDescription", "(Ljava/lang/String;)V"), env->NewStringUTF(wallet.get_description().c_str()));
    } catch (const std::exception &e) {
        syslog(LOG_DEBUG, "[JNI] convert2JWallet error::%s", e.what());
        Deserializer::convert2JException(env, e.what());
    }
    return instance;
}

jobject Deserializer::convert2JWallets(JNIEnv *env, const std::vector<Wallet> &wallets) {
    syslog(LOG_DEBUG, "[JNI] convert2JWallets()");
    static auto arrayListClass = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    static jmethodID constructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayListInstance = env->NewObject(arrayListClass, constructor);
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    if (wallets.empty()) {
        syslog(LOG_DEBUG, "[JNI] wallets empty");
        return arrayListInstance;
    } else {
        syslog(LOG_DEBUG, "[JNI] wallets size:: %lu", wallets.size());
        for (const Wallet &s: wallets) {
            jobject element = convert2JWallet(env, s);
            env->CallBooleanMethod(arrayListInstance, addMethod, element);
            env->DeleteLocalRef(element);
        }
    }
    return arrayListInstance;
}

void Deserializer::convert2JException(JNIEnv *env, const char *msg) {
    jclass clazz = env->FindClass("com/nunchuk/android/exception/NunchukNativeException");
    if (nullptr == clazz) {
        clazz = env->FindClass("java/lang/NullPointerException");
    }
    env->ThrowNew(clazz, msg);
}