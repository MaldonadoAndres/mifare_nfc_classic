package com.example.mifare_nfc_classic

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.IOException


private const val TAG = "MifareNfcClassicPlugin"

class MifareNfcClassicPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var activity: Activity
    private lateinit var channel: MethodChannel
    private lateinit var mNfcAdapter: NfcAdapter
    private lateinit var mifareClassic: MifareClassic

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
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "read" -> {
                readMifareCard(result)
            }
            "write" -> {
                writeMifare(result)
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


    private fun readMifareCard(result: Result) {

        mNfcAdapter.enableReaderMode(activity, { tag ->
            mNfcAdapter.disableReaderMode(activity)
            activity.runOnUiThread { result.success(readFromNFC(tag)) }


        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    private fun readFromNFC(tag: Tag): String {
        val blocks = arrayListOf<String>()
        try {
            mifareClassic = MifareClassic.get(tag)
            mifareClassic.connect()
            mifareClassic.authenticateSectorWithKeyA(2, MifareClassic.KEY_DEFAULT)
            val firstBlock: Int = mifareClassic.sectorToBlock(2)
            val lastBlock = firstBlock + 4
            for (i in firstBlock until lastBlock) {
                try {
                    var blockBytes: ByteArray = mifareClassic.readBlock(i)
                    if (blockBytes.size < 16) {
                        throw IOException()
                    }
                    if (blockBytes.size > 16) {
                        blockBytes = blockBytes.copyOf(16)
                    }
                    val hex = Utils.byte2Hex(blockBytes)
                    blocks.add(hex!!)
                    Log.d(TAG, "readFromNFC: $hex")
                } catch (e: Exception) {

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "readFromNFC: ", e)
        } finally {
            mifareClassic.close()
        }
        return blocks[0]

    }

    private fun writeMifare(result: Result) {
        mNfcAdapter.enableReaderMode(activity, { tag ->
            try {
                mifareClassic = MifareClassic.get(tag)
                mifareClassic.connect()
                mifareClassic.authenticateSectorWithKeyA(2, MifareClassic.KEY_DEFAULT)
                mifareClassic.writeBlock(
                        9,
                        Utils.hex2ByteArray("FAC0FAC000000000FAC0000000000000")
                )
            } catch (e: Exception) {
                Log.e(TAG, "writeMifare: ", e)
            } finally {
                mifareClassic.close()
                mNfcAdapter.disableReaderMode(activity)
            }
            activity.runOnUiThread { result.success(true) }
        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }
}
