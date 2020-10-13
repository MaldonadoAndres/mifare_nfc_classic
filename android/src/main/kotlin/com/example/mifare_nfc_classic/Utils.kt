package com.example.mifare_nfc_classic

import android.util.Log

private const val TAG = "Utils"

object Utils {

    fun byte2Hex(bytes: ByteArray?): String? {
        val ret = StringBuilder()
        if (bytes != null) {
            for (b in bytes) {
                ret.append(String.format("%02X", b.toInt() and 0xFF))
            }
        }
        return ret.toString()
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

}