package com.example.nfc_classic_mifare

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


private const val TAG = "NfcClassicMifarePlugin"

class NfcClassicMifarePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var activity: Activity
    private lateinit var channel: MethodChannel
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var mifareClassic: MifareClassic
    private val flag = NfcAdapter.FLAG_READER_NFC_A

    companion object {
        private const val CHANNEL_NAME = "nfc_classic_mifare'"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val instance = NfcClassicMifarePlugin()
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            instance.channel = channel
            instance.mNfcAdapter = NfcAdapter.getDefaultAdapter(registrar.context())
            instance.activity = registrar.activity() as Activity
            channel.setMethodCallHandler(NfcClassicMifarePlugin())
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
        val passwordA: String? = call.argument<String>("passwordA")
        val passwordB: String? = call.argument<String>("passwordB")
        val blockIndex: Int? = call.argument<Int>("blockIndex")
        val sectorIndex: Int? = call.argument<Int>("sectorIndex")
        val message: String? = call.argument<String>("message")
        when (call.method) {
            "readBlock" -> {
                readBlock(result = result, blockIndex = blockIndex!!, passwordA = passwordA, passwordB = passwordB)
            }
            "writeBlock" -> {
                writeBlock(result = result, blockIndex = blockIndex!!, message = message!!, passwordA = passwordA, passwordB = passwordB)
            }
            "changePasswordOfSector" -> {
                val newPasswordA = call.argument<String>("newPasswordA")!!
                val newPasswordB = call.argument<String>("newPasswordB")
                changePasswordOfSector(result = result, sectorIndex = sectorIndex!!, passwordA = passwordA, passwordB = passwordB, newPasswordA = newPasswordA, newPasswordB = newPasswordB)
            }
            "overwriteBlock" -> {
                overwriteBlock(result = result, passwordA = passwordA, passwordB = passwordB, blockIndex = blockIndex!!, message = message!!)
            }
            "writeRawHexToBlock" -> {
                writeRawHexToBlock(result = result, blockIndex = blockIndex!!, message = message!!, passwordA = passwordA, passwordB = passwordB)
            }
            "readSector" -> {
                readSector(result = result, sectorIndex = sectorIndex!!, passwordA = passwordA, passwordB = passwordB)
            }
            "sectorCount" -> sectorCount(result = result)
            "blockCount" -> blockCount(result = result)
            "isNFCEnabled" -> isNFCEnabled(result = result)
            "readAll" -> {
                readAll(result = result, passwordA = passwordA, passwordB = passwordB)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "nfc_classic_mifare")
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

    private fun readBlock(result: Result, blockIndex: Int, passwordA: String?, passwordB: String?) {
        fun tryRead(blockIndex: Int, result: Result) {
            var blockBytes = mifareClassic.readBlock(blockIndex)
            if (blockBytes.size < 16) {
                throw IOException()
            }
            if (blockBytes.size > 16) {
                blockBytes = blockBytes.copyOf(16)
            }
            Log.d(TAG, "readBlock: ${Utils.byteArray2Hex(blockBytes)}")
            activity.runOnUiThread { result.success(Utils.byteArray2Hex(blockBytes)) }
        }
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                val blockAccessConditions = resolveAccessConditionsForBlock(blockIndex, keyA = passwordA, mifareClassic = mifareClassic)
                when(blockAccessConditions.readKey) {
                    AccessType.Never ->  activity.runOnUiThread { result.error("401", "This block is not readable", null) }
                    AccessType.KeyA -> {
                        if(!passwordA.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyA(sectorIndex, Utils.hex2ByteArray(passwordA))
                            tryRead(blockIndex, result)
                        } else {
                            activity.runOnUiThread {
                                result.error(
                                    "401",
                                    "Only KeyA can read this block",
                                    null
                                )
                            }

                        }
                    }
                    AccessType.KeyB -> {
                        if(!passwordB.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyB(sectorIndex, Utils.hex2ByteArray(passwordB))
                            tryRead(blockIndex, result)
                        } else {
                            activity.runOnUiThread {
                                result.error(
                                    "401",
                                    "Only KeyB can read this block",
                                    null
                                )
                            }
                        }
                    }
                    AccessType.KeyAB -> {
                        if(passwordA.isNullOrEmpty() && passwordB.isNullOrEmpty()) {
                            val isAuthenticated = mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)
                            if(!isAuthenticated) {
                                activity.runOnUiThread {
                                    result.error(
                                        "401",
                                        "KeyA or KeyB should be provided for this block",
                                        null
                                    )
                                }
                            }
                        } else if(!passwordA.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyA(sectorIndex, Utils.hex2ByteArray(passwordA))
                            tryRead(blockIndex, result)
                        } else {
                            mifareClassic.authenticateSectorWithKeyB(sectorIndex, Utils.hex2ByteArray(passwordB))
                            tryRead(blockIndex, result)
                        }
                    }
                }
            } catch (e: Exception) {
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun writeBlock(result: Result, blockIndex: Int, message: String, passwordA: String?, passwordB: String?) {
        var messageAsHex = Utils.byteArray2Hex(message.toByteArray())
        val diff = 32 - messageAsHex.length
        messageAsHex = "$messageAsHex${"0".repeat(diff)}"
        // Log.d(TAG, "writeBlockOfSector: $messageAsHex")
        return writeRawHexToBlock(result, blockIndex, messageAsHex, passwordA, passwordB)
    }

    private fun overwriteBlock(result: Result, blockIndex: Int, message: String, passwordA: String?, passwordB: String?) {
        var toWrite: ByteArray = byteArrayOf()
        var tagId: String? = null
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (passwordA.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = passwordA)
                }
                Log.d(TAG, "overwriteBlock: $message")

                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                tagId = Utils.byteArray2Hex(tag.id)
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
            resultMap["minutes"] = Utils.byteArray2Hex(toWrite)
            resultMap["cardId"] = tagId!!
            activity.runOnUiThread { result.success(resultMap) }
        }, flag, null)


    }

    //TODO Write to both sides
    private fun changePasswordOfSector(result: Result, sectorIndex: Int, newPasswordA: String, newPasswordB: String?, passwordA: String?, passwordB: String?) {
        var didWrite = true
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (passwordA.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = passwordA)
                }
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val blockIndex = mifareClassic.sectorToBlock(sectorIndex) + 3
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, sectorPassword)
                val toWrite = mifareClassic.readBlock(blockIndex)
                val decList: ArrayList<Int> = arrayListOf()
                for (i in newPasswordA.indices step 2) {
                    val temp = "${newPasswordA[i]}${newPasswordA[i + 1]}"
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

    private fun writeRawHexToBlock(result: Result, blockIndex: Int, message: String, passwordA: String?, passwordB: String?) {
        var didWrite = true
        fun tryWrite(blockIndex: Int) {
            val toWrite = Utils.rawHexToByteArray(hex = message)
            Log.d(TAG, "writeRawBlockOfSector: ${toWrite.size}")
            Log.d(TAG, "writeRawBlockOfSector: $toWrite")
            mifareClassic.writeBlock(
                blockIndex,
                toWrite
            )
        }
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                val sectorIndex = mifareClassic.blockToSector(blockIndex)
                val blockAccessConditions = resolveAccessConditionsForBlock(blockIndex, keyA = passwordA, mifareClassic = mifareClassic)
                when(blockAccessConditions.writeKey) {
                    AccessType.Never ->  activity.runOnUiThread { result.error("401", "This block is not readable", null) }
                    AccessType.KeyA -> {
                        if(!passwordA.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyA(sectorIndex, Utils.hex2ByteArray(passwordA))
                            tryWrite(blockIndex)
                        } else {
                            activity.runOnUiThread {
                                result.error(
                                    "401",
                                    "Only KeyA can write to this block",
                                    null
                                )
                            }
                        }
                    }
                    AccessType.KeyB -> {
                        if(!passwordB.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyB(sectorIndex, Utils.hex2ByteArray(passwordB))
                            tryWrite(blockIndex)
                        } else {
                            activity.runOnUiThread {
                                result.error(
                                    "401",
                                    "Only KeyB can write to this block",
                                    null
                                )
                            }
                        }
                    }
                    AccessType.KeyAB -> {
                        if(passwordA.isNullOrEmpty() && passwordB.isNullOrEmpty()) {
                            val isAuthenticated = mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)
                            if(!isAuthenticated) {
                                activity.runOnUiThread {
                                    result.error(
                                        "401",
                                        "KeyA or KeyB should be provided for this block",
                                        null
                                    )
                                }
                            }
                        } else if(!passwordA.isNullOrEmpty()) {
                            mifareClassic.authenticateSectorWithKeyA(sectorIndex, Utils.hex2ByteArray(passwordA))
                            tryWrite(blockIndex)
                        } else {
                            mifareClassic.authenticateSectorWithKeyB(sectorIndex, Utils.hex2ByteArray(passwordB))
                            tryWrite(blockIndex)
                        }
                    }
                }
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

    private fun readSector(result: Result, sectorIndex: Int, passwordA: String?, passwordB: String?) {
        Log.d(TAG, "readSector: Sector Index -> $sectorIndex")
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (passwordA.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = passwordA)
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

    private fun readAll(result: Result, passwordA: String?, passwordB: String?) {
        Log.d(TAG, "readAll")
        val response = mutableMapOf<Int, List<String>>()
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                val sectorPassword: ByteArray = if (passwordA.isNullOrEmpty()) {
                    MifareClassic.KEY_DEFAULT
                } else {
                    Utils.rawHexToByteArray(hex = passwordA)
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

    private fun resolveAccessConditionsForSector(sectorIndex: Int, keyA: String?, mifareClassic: MifareClassic): SectorAccessConditions  {
        var accessConditions = SectorAccessConditions.Common.NEVER
        try {
            val key: String = if(keyA.isNullOrEmpty()) { Utils.byteArray2Hex(bytes = MifareClassic.KEY_DEFAULT) } else keyA
            val block = mifareClassic.sectorToBlock(sectorIndex)
            val isAuthenticated = mifareClassic.authenticateSectorWithKeyA(sectorIndex, Utils.hex2ByteArray(key))
            if(!isAuthenticated) return accessConditions
            val aclBlock = mifareClassic.readBlock(block + 3)
            val acl = aclBlock.copyOfRange(6, 9)
            // Log.d("acl", Utils.byteArray2Hex(acl))
            accessConditions = Utils.resolveSectorAccessConditions(acl)
        } catch (e: Exception) {
            Log.d(TAG, e.localizedMessage)
        }
        return accessConditions
    }

    private fun resolveAccessConditionsForBlock(blockIndex: Int, keyA: String?, mifareClassic: MifareClassic) : DataBlockAccessConditions {
        val sectorIndex = mifareClassic.blockToSector(blockIndex)
        val firstBlockOfSector = mifareClassic.sectorToBlock(sectorIndex)
        val accessConditions = resolveAccessConditionsForSector(sectorIndex, keyA, mifareClassic = mifareClassic)
        if(blockIndex == firstBlockOfSector) {
            return accessConditions.block0
        } else if(blockIndex == firstBlockOfSector + 1) {
            return accessConditions.block1
        }
        return accessConditions.block2
    }
}
