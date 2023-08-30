/**
 * Access bits decoded according to 'Chapter 4: Access Bits and Conditions' of this document
 * https://shop.sonmicro.com/Downloads/MIFARECLASSIC-UM.pdf
 */

package com.example.nfc_classic_mifare

class SectorBlockAccessConditions constructor() : DataBlockAccessConditions() {
    val keyARead : AccessType = AccessType.Never
    var keyAWrite : AccessType = AccessType.Never

    var accessBitsRead : AccessType = AccessType.KeyAB
    var accessBitsWrite : AccessType = AccessType.Never

    var keyBRead : AccessType = AccessType.Never
    var keyBWrite : AccessType = AccessType.Never
    constructor(C1: Boolean = true, C2: Boolean = true, C3: Boolean = true) : this() {
        resolveAccessConditionsForKeyA(C1,C2,C3)
        resolveAccessConditionsForAccessBits(C1,C2,C3)
        resolveAccessConditionsForKeyB(C1,C2,C3)
    }

    private fun resolveAccessConditionsForKeyA(C1: Boolean, C2: Boolean, C3: Boolean) {
        if(!(C1 && C2 && C3) || (!(C1 && C2) && C3)) {
            keyAWrite = AccessType.KeyA
        }
        if((!(C2 && C3) && C1) || ((C2 && C3) && !C1)) {
            keyAWrite = AccessType.KeyB
        }
    }

    private fun resolveAccessConditionsForAccessBits(C1: Boolean, C2: Boolean, C3: Boolean) {
        if(!(C1 && C2 && C3) || (!(C1 && C3) && C2) || (!(C1 && C2) && C3)) {
            accessBitsRead = AccessType.KeyA
        }
        if((!(C1 && C2) && C3)) {
            accessBitsWrite = AccessType.KeyA
        }
        if((!C1 && (C2 && C3)) || (!C2 && (C1 && C3))) {
            accessBitsWrite = AccessType.KeyB
        }
    }

    private fun resolveAccessConditionsForKeyB(C1: Boolean, C2: Boolean, C3: Boolean) {
        if(accessBitsRead == AccessType.KeyA) {
            keyBRead = AccessType.KeyA
        }
        if(accessBitsRead == AccessType.KeyA && C2) {
            keyBWrite = AccessType.Never
        }
        if((!(C2 && C3) && C1) || (!C1 && (C2 && C3))) {
            keyBWrite = AccessType.KeyB
        }
    }
}