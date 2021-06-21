package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    val TAG = this.javaClass.name
    private val scope: CoroutineScope = MainScope()

    override fun onReceive(context: Context?, intent: Intent) {

        Log.d(TAG, "onReceive action: " + intent.action)


        scope.launch {
            // TODO: Request APIs upon boot complete

        }
    }
}