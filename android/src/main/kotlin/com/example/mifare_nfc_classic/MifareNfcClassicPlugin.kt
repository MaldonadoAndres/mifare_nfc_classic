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
        when (call.method) {
            "readBlockOfSector" -> {
                val blockIndex = call.argument<Int>("blockIndex")!!
                val sectorIndex = call.argument<Int>("sectorIndex")!!
                readBlockOfSector(result = result, blockIndex = blockIndex, sectorIndex = sectorIndex)
            }
            "writeBlockOfSector" -> {
                val blockIndex = call.argument<Int>("blockIndex")!!
                val sectorIndex = call.argument<Int>("sectorIndex")!!
                val message = call.argument<String>("message")!!
                writeBlockOfSector(result = result, blockIndex = blockIndex, sectorIndex = sectorIndex, message = message)
            }
            "readSector" -> {
                val sectorIndex = call.argument<Int>("sectorIndex")!!
                readSector(result = result, sectorIndex = sectorIndex)
            }
            "sectorCount" -> sectorCount(result = result)
            "blockCount" -> blockCount(result = result)
            "isNFCEnabled" -> isNFCEnabled(result = result)
            "readAll" -> readAll(result = result)
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

    private fun readBlockOfSector(result: Result, sectorIndex: Int, blockIndex: Int) {
        Log.d(TAG, "readBlockOfSector: Sector Index -> $sectorIndex Block Index -> $blockIndex")
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)
                var blockBytes = mifareClassic.readBlock(blockIndex)
                if (blockBytes.size < 16) {
                    throw IOException()
                }
                if (blockBytes.size > 16) {
                    blockBytes = blockBytes.copyOf(16)
                }
                Log.d(TAG, "readBlock: ${Utils.byte2Hex(blockBytes)}")
                activity.runOnUiThread { result.success(Utils.byte2Hex(blockBytes)) }
            } catch (e: Exception) {
                activity.runOnUiThread { result.error("404", e.localizedMessage, null) }
            } finally {
                mifareClassic.close()
                mNfcAdapter?.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun readSector(result: Result, sectorIndex: Int) {
        Log.d(TAG, "readSector: Sector Index -> $sectorIndex")
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)
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

    private fun readAll(result: Result) {
        Log.d(TAG, "readAll")
        val response = mutableMapOf<Int, List<String>>()
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                for (i in 0 until mifareClassic.sectorCount) {
                    mifareClassic.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)
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

    private fun writeBlockOfSector(result: Result, sectorIndex: Int, blockIndex: Int, message: String) {
        var didWrite = true
        mNfcAdapter?.enableReaderMode(activity, { tag ->
            try {
                var messageAsHex = Utils.byte2Hex(message.toByteArray())
                val diff = 32 - messageAsHex!!.length
                messageAsHex = "$messageAsHex${"0".repeat(diff)}"
                Log.d(TAG, "writeBlockOfSector: $messageAsHex")
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)
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
