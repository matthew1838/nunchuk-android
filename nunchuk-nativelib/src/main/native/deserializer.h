#include <cstring>
#include <jni.h>
#include <syslog.h>
#include <nunchuk.h>

using namespace nunchuk;

/**
 * Used to convert C++ Classes into Java models
 */
class Deserializer {

public:

    static void convert2JException(JNIEnv *env, const char *msg);

    static jobject convert2JAddressType(JNIEnv *env, const AddressType &type);

    static jobject convert2JSigner(JNIEnv *env, const SingleSigner &signer);

    static jobject convert2JSigners(JNIEnv *env, const std::vector<SingleSigner> &signers);

    static jobject convert2JWallet(JNIEnv *env, const Wallet &wallet);

    static jobject convert2JWallets(JNIEnv *env, const std::vector<Wallet> &wallets);
};