package com.github.thibaultbee.srtdroid.enums

enum class KMState {
    KM_S_UNSECURED,      //No encryption
    KM_S_SECURING,       //Stream encrypted, exchanging Keying Material
    KM_S_SECURED,        //Stream encrypted, keying Material exchanged, decrypting ok.
    KM_S_NOSECRET,       //Stream encrypted and no secret to decrypt Keying Material
    KM_S_BADSECRET       //Stream encrypted and wrong secret, cannot decrypt Keying Material
}