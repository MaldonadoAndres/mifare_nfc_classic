package com.example.mifare_nfc_classic

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.MifareClassic
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.IOException


private const val TAG = "MifareNfcClassicPlugin"

class MifareNfcClassicPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var activity: Activity
    private lateinit var channel: MethodChannel
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var mifareClassic: MifareClassic
    private val flag = NfcAdapter.FLAG_READER_NFC_A

    companion object {
        private const val CHANNEL_NAME = "mifare_nfc_classic'"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val instance = MifareNfcClassicPlugin()
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            instance.channel = channel
            instance.mNfcAdapter = NfcAdapter.getDefaultAdapter(registrar.context())
            instance.activity = registrar.activity()
            channel.setMethodCallHandler(MifareNfcClassicPlugin())
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val password: String? = call.argument<String>("password")
        val blockIndex: Int? = call.argument<Int>("blockIndex")
        val sectorIndex: Int? = call.argument<Int>("sectorIndex")
        val message: String? = call.argument<String>("message")
        when (call.method) {
            "readBlock" -> {
                readBlock(result = result, blockIndex = blockIndex!!, password = password)
            }
            "writeBlock" -> {
                writeBlock(result = result, blockIndex = blockIndex!!, message = message!!, password = password)
            }
            "changePasswordOfSector" -> {
                val newPassword = call.argument<String>("newPassword")!!
                changePasswordOfSector(result = result, sectorIndex = sectorIndex!!, password = password, newPassword = newPassword)
            }
            "overwriteBlock" -> {
                overwriteBlock(result = result, password = password, blockIndex = blockIndex!!, message = message!!)
            }
            "writeRawHexToBlock" -> {
                writeRawHexToBlock(result = result, blockIndex = blockIndex!!, message = message!!, password = password)
            }
            "readSector" -> {
                readSector(result = result, sectorIndex = sectorIndex!!, password = password)
            }
            "sectorCount" -> sectorCount(result = result)
            "blockCount" -> blockCount(result = result)
            "isNFCEnabled" -> isNFCEnabled(result = result)
            "readAll" -> {
                readAll(result = result, password = password)
            }
            else -> {
                result.notImplemented()
            }
        }


    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "mifare_nfc_classic")
        channel.setMethodCallHandler(this)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(flutterPluginBinding.applicationContext)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun isNFCEnabled(result: Result) {
        val message: String = if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {
            "AVAILABLE"
        } else if (mNfcAdapter != null && !mNfcAdapter!!.isEnabled) {
            "NOT_ENABLED"
        } else {
            "NOT_SUPPORTED"
        }
        result.success(message)
    }

    private fun readBlock(result: Result, blockIndex: Int, password: String?) {
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }

                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                var blockBytes = mifareClassic.readBlock(blockIndex)
                if (blockBytes.size < 16) {
                    throw IOException()
                }
                if (blockBytes.size > 16) {
                    blockBytes = blockBytes.copyOf(16)
                }
                Log.d(TAG, "readBlock: ${Utils.byteArray2Hex(blockBytes)}")
                activity.runOnUiThread { result.success(Utils.byteArray2Hex(blockBytes)) }
            } catch (e: Exception) {
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun writeBlock(result: Result, blockIndex: Int, message: String, password: String?) {
        var didWrite = true
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                var messageAsHex = Utils.byteArray2Hex(message.toByteArray())
                val diff = 32 - messageAsHex!!.length
                messageAsHex = "$messageAsHex${"0".repeat(diff)}"
                Log.d(TAG, "writeBlockOfSector: $messageAsHex")
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                mifareClassic.writeBlock(
                        blockIndex,
                        Utils.hex2ByteArray(messageAsHex)
                )
            } catch (e: Exception) {
                didWrite = false
                Log.e(TAG, "writeMifare: ", e)
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
            activity.runOnUiThread { result.success(didWrite) }
        }, flag, null)
    }

    private fun overwriteBlock(result: Result, blockIndex: Int, message: String, password: String?) {
        var toWrite: ByteArray = byteArrayOf()
        var tagId: String? = null
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                Log.d(TAG, "overwriteBlock: $message")

                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                tagId = Utils.byteArray2Hex(tag.id)!!
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                toWrite = mifareClassic.readBlock(blockIndex)
                val decList: ArrayList<Int> = arrayListOf()
                for (i in message.indices step 2) {
                    val temp = "${message[i]}${message[i + 1]}"
                    decList.add(temp.toInt(radix = 16))
                }
                for (i in decList.indices) {
                    toWrite[i] = (decList[i] + toWrite[i].toInt()).toByte()
                }
                mifareClassic.writeBlock(
                        blockIndex,
                        toWrite
                )

            } catch (e: Exception) {
                Log.e(TAG, "writeMifare: ", e)
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
            // val resultMap = mapOf("minutes" to Utils.byteArray2Hex(toWrite)!!, "cardId" to tagId)
            val resultMap = mutableMapOf<String, String>()
            resultMap["minutes"] = Utils.byteArray2Hex(toWrite)!!
            resultMap["cardId"] = tagId!!
            activity.runOnUiThread { result.success(resultMap) }
        }, flag, null)


    }

    //TODO Write to both sides
    private fun changePasswordOfSector(result: Result, sectorIndex: Int, newPassword: String, password: String?) {
        var didWrite = true
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val blockIndex = mifareClassic.sectorToBlock(sectorIndex) + 3
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                val toWrite = mifareClassic.readBlock(blockIndex)
                val decList: ArrayList<Int> = arrayListOf()
                for (i in newPassword.indices step 2) {
                    val temp = "${newPassword[i]}${newPassword[i + 1]}"
                    decList.add(temp.toInt(radix = 16))
                }

                for (i in decList.indices) {
                    toWrite[i] = decList[i].toByte()
                    toWrite[10 + i] = decList[i].toByte()
                }
                mifareClassic.writeBlock(
                        blockIndex,
                        toWrite
                )
            } catch (e: Exception) {
                didWrite = false
                Log.e(TAG, "writeMifare: ", e)
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
            activity.runOnUiThread { result.success(didWrite) }
        }, flag, null)


    }

    private fun writeRawHexToBlock(result: Result, blockIndex: Int, message: String, password: String?) {
        var didWrite = true
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                Log.d(TAG, "writeRawBlockOfSector: $sectorIndex $blockIndex")
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                val toWrite = Utils.rawHexToByteArray(hex = message)
                Log.d(TAG, "writeRawBlockOfSector: ${toWrite.size}")
                Log.d(TAG, "writeRawBlockOfSector: $toWrite")
                mifareClassic.writeBlock(
                        blockIndex,
                        toWrite
                )

            } catch (e: Exception) {
                didWrite = false
                Log.e(TAG, "writeMifare: ", e)
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
            activity.runOnUiThread { result.success(didWrite) }
        }, flag, null)
    }

    private fun readSector(result: Result, sectorIndex: Int, password: String?) {
        Log.d(TAG, "readSector: Sector Index -> $sectorIndex")
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                val sector = Utils.printEntireBlock(mifareClassic, sectorIndex)
                activity.runOnUiThread { result.success(sector) }
            } catch (e: Exception) {
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun readAll(result: Result, password: String?) {
        Log.d(TAG, "readAll")
        val response = mutableMapOf<Int, List<String>>()
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (password.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = password)
                }
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                for (i in 0 until mifareClassic.sectorCount) {
                    mifareClassic.authenticateSectorWithKeyA(i, sectorPassword)
                    response[i] = Utils.printEntireBlock(mifareClassic, i)
                }
                activity.runOnUiThread { result.success(response) }

            } catch (e: Exception) {
                Log.e(TAG, "readAll: ", e)
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }

            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun sectorCount(result: Result) {
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                activity.runOnUiThread { result.success(mifareClassic.sectorCount) }
            } catch (e: Exception) {
                Log.e(TAG, "writeMifare: ", e)
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun blockCount(result: Result) {
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                activity.runOnUiThread { result.success(mifareClassic.blockCount) }
            } catch (e: Exception) {
                Log.e(TAG, "writeMifare: ", e)
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }
}
