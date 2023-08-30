/**
 * Access bits decoded according to 'Chapter 4: Access Bits and Conditions' of this document
 * https://shop.sonmicro.com/Downloads/MIFARECLASSIC-UM.pdf
 */

package com.example.nfc_classic_mifare

open class  DataBlockAccessConditions(C1: Boolean = true, C2: Boolean = true, C3: Boolean = true) {
    var writeKey: AccessType = AccessType.Never
    var readKey: AccessType = AccessType.KeyAB

    init {
        resolveAccessConditionsForDataBlock(C1,C2, C3)
    }

    private fun resolveAccessConditionsForDataBlock(C1:Boolean, C2:Boolean, C3: Boolean) {
        if((C1 && C3) || (C2 && C3)) {
            readKey = AccessType.KeyB
        } else if(C1 && C2 && C3) {
            readKey = AccessType.Never
        }

        if(!(C1 && C2 && C3)) {
            writeKey = AccessType.KeyAB
        }
        if((C1 && !C2 && !C3) || (C1 && C2 && !C3) || (!C1 && C2 && C3)) {
            writeKey = AccessType.KeyB
        }
    }
}