package com.goel.peerlocator.viewmodels

import androidx.lifecycle.ViewModel
import com.goel.peerlocator.listeners.BlockListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.BlockRepository
import com.google.firebase.firestore.DocumentReference

class BlockListViewModel : ViewModel() {

    val blockList : ArrayList<UnknownUserModel> = ArrayList()
    val checkedList = ArrayList<UnknownUserModel>()

    fun getMyBlockList (listener : BlockListener) {
        BlockRepository.instance.getMyBlockList(listener)
    }

    fun unblockSelected (listener: BlockListener) {
        if (checkedList.isNotEmpty()) {
            val list = ArrayList<DocumentReference>()
            for (model in checkedList) {
                list.add(model.documentReference)
            }
            BlockRepository.instance.unblockSelected(list, listener)
        }
    }
}