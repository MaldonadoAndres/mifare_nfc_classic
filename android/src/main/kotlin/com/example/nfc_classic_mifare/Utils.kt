package com.example.nfc_classic_mifare

import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException

private const val TAG = "Utils"

object Utils {

    fun byteArray2Hex(bytes: ByteArray?): String {
        val ret = StringBuilder()
        if (bytes != null) {
            for (b in bytes) {
                ret.append(String.format("%02X", b.toInt() and 0xFF))
            }
        }
        return ret.toString()
    }

    fun byteToHex(byte: Byte): String {
        return String.format("%02X", byte.toInt() and 0xFF)
    }


    fun hex2ByteArray(hex: String?): ByteArray? {
        val regex = Regex("[0-9A-Fa-f]+")
        if (!(hex != null && hex.length % 2 == 0 && hex.matches(regex))) {
            return null
        }
        val len = hex.length
        val data = ByteArray(len / 2)
        try {
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4)
                        + Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
        } catch (e: java.lang.Exception) {
            Log.d(
                    TAG, "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string"
            )
        }
        return data
    }

    fun rawHexToByteArray(hex: String): ByteArray {
       // Log.d(TAG, "rawHexToByteArray: Converting $hex")
        val toWrite = ByteArray(16)
        val decList: ArrayList<Int> = arrayListOf()
        for (i in hex.indices step 2) {
            val temp = "${hex[i]}${hex[i + 1]}"
            decList.add(temp.toInt(radix = 16))
        }
        //Log.d(TAG, "rawHexToByteArray: $decList")
        //Log.d(TAG, "rawHexToByteArray: $toWrite")
        for (i in decList.indices) {
            toWrite[i] = decList[i].toByte()
        }
        return toWrite
    }

    fun printEntireBlock(mifareClassic: MifareClassic, sectorIndex: Int): ArrayList<String> {
        val sectorAsHex = arrayListOf<String>()
        val firstBlock: Int = mifareClassic.sectorToBlock(sectorIndex)
        val lastBlock = firstBlock + 4
        //Log.d(TAG, "printEntireBlock: Range First Block -> $firstBlock Last Block -> $lastBlock")
        for (i in firstBlock until lastBlock) {
            try {
                var blockBytes: ByteArray = mifareClassic.readBlock(i)
                if (blockBytes.size < 16) {
                    throw IOException()
                }
                if (blockBytes.size > 16) {
                    blockBytes = blockBytes.copyOf(16)
                }
                val hex = byteArray2Hex(blockBytes)
                sectorAsHex.add(hex)
                // Log.d(TAG, "Printing Block: $hex")
            } catch (e: Exception) {

            }
        }
        return sectorAsHex
    }

    fun resolveSectorAccessConditions(accessConditionsBlock: ByteArray = byteArrayOf(0.toByte(), 240.toByte(), 255.toByte())): SectorAccessConditions {
        val block0: DataBlockAccessConditions
        val block1: DataBlockAccessConditions
        val block2: DataBlockAccessConditions
        val sectorBlock: SectorBlockAccessConditions

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

        return SectorAccessConditions(block0, block1, block2, sectorBlock)
    }
}