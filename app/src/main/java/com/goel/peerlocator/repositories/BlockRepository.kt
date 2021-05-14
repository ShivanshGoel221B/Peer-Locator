package com.goel.peerlocator.repositories

import com.goel.peerlocator.listeners.BlockListener
import com.goel.peerlocator.utils.firebase.database.Database
import com.google.firebase.firestore.DocumentReference

class BlockRepository {
    companion object {
        val instance = BlockRepository()
    }

    fun getMyBlockList (listener: BlockListener) {
        Database.getMyBlockList(listener)
    }

    fun unblockSelected (list: List<DocumentReference>, listener: BlockListener) {
        val pathList = ArrayList<String>()
        for (block in list) {
            pathList.add(block.path)
        }
        Database.unblockSelected(pathList, listener)
    }
}