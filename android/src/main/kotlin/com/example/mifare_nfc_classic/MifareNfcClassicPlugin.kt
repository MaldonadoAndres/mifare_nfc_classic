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
    private lateinit var mNfcAdapter: NfcAdapter
    private lateinit var mifareClassic: MifareClassic
    private val flag = NfcAdapter.FLAG_READER_NFC_A

    companion object {
        const val CHANNEL_NAME = "mifare_nfc_classic'"

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

    private fun readBlockOfSector(result: Result, sectorIndex: Int, blockIndex: Int) {
        Log.d(TAG, "readBlockOfSector: Sector Index -> $sectorIndex Block Index -> $blockIndex")
        mNfcAdapter.enableReaderMode(activity, { tag ->
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
                mNfcAdapter.disableReaderMode(activity)
            }
        }, flag, null)
    }

    private fun readSector(result: Result, sectorIndex: Int) {
        Log.d(TAG, "readSector: Sector Index -> $sectorIndex")
        mNfcAdapter.enableReaderMode(activity, { tag ->
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
                mNfcAdapter.disableReaderMode(activity)
            }
        }, flag, null)
    }


    private fun writeBlockOfSector(result: Result, sectorIndex: Int, blockIndex: Int, message: String) {
        var didWrite = true
        mNfcAdapter.enableReaderMode(activity, { tag ->
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
                mNfcAdapter.disableReaderMode(activity)
            }
            activity.runOnUiThread { result.success(didWrite) }
        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }
}
