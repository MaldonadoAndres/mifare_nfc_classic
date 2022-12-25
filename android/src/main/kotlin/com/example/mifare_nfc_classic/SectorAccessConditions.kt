package com.example.mifare_nfc_classic

class SectorAccessConditions(
    val block0: DataBlockAccessConditions,
    val block1: DataBlockAccessConditions,
    val block2: DataBlockAccessConditions,
    val sectorBlock: SectorBlockAccessConditions
) {
    object Common {
        val NEVER = Utils.resolveSectorAccessConditions()
    }
}