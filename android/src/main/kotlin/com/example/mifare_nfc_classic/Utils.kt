package com.example.mifare_nfc_classic

import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException

private const val TAG = "Utils"

object Utils {

    fun byteArray2Hex(bytes: ByteArray?): String? {
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
                sectorAsHex.add(hex!!)
                // Log.d(TAG, "Printing Block: $hex")
            } catch (e: Exception) {

            }
        }
        return sectorAsHex
    }

}