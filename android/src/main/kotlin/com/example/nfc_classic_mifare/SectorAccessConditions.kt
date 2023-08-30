package com.example.nfc_classic_mifare

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