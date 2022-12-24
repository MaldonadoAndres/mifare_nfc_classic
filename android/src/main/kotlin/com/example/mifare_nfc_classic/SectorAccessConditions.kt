/**
 * Access bits decoded according to 'Chapter 4: Access Bits and Conditions' of this document
 * https://shop.sonmicro.com/Downloads/MIFARECLASSIC-UM.pdf
 */

package com.example.mifare_nfc_classic

internal class SectorBlockAccessConditions constructor() : DataBlockAccessConditions() {
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

internal class SectorAccessConditions
    (accessConditionsBlock: ByteArray = byteArrayOf(0.toByte(), 240.toByte(), 255.toByte())) {
    var block0: DataBlockAccessConditions = DataBlockAccessConditions()
    var block1: DataBlockAccessConditions = DataBlockAccessConditions()
    var block2: DataBlockAccessConditions = DataBlockAccessConditions()
    var sectorBlock: SectorBlockAccessConditions = SectorBlockAccessConditions()

    init {
        val acByteC1 = Integer.toBinaryString(accessConditionsBlock[1].toInt()).padStart(8,'0').map { it == '1' }.take(4)
        val acByteC2 = Integer.toBinaryString(accessConditionsBlock[2].toInt()).padStart(8,'0').map { it == '1' }.take(4)
        val acByteC3 = Integer.toBinaryString(accessConditionsBlock[2].toInt()).padStart(8,'0').map { it == '1' }.takeLast(4)
        val block0C1 = acByteC1[3]
        val block0C2 = acByteC2[3]
        val block0C3 = acByteC3[3]
        block0 = DataBlockAccessConditions(block0C1, block0C2, block0C3)
        val block1C1 = acByteC1[2]
        val block1C2 = acByteC2[2]
        val block1C3 = acByteC3[2]
        block1 = DataBlockAccessConditions(block1C1, block1C2, block1C3)
        val block2C1 = acByteC1[1]
        val block2C2 = acByteC2[1]
        val block2C3 = acByteC3[1]
        block2 = DataBlockAccessConditions(block2C1, block2C2, block2C3)
        val sectorBlockC1 = acByteC1[0]
        val sectorBlockC2 = acByteC2[0]
        val sectorBlockC3 = acByteC3[0]
        sectorBlock = SectorBlockAccessConditions(sectorBlockC1, sectorBlockC2, sectorBlockC3)
    }
}

enum class AccessType {
    Never, KeyA, KeyB, KeyAB
}