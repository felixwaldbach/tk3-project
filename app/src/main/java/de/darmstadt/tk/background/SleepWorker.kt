package de.darmstadt.tk.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SleepWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams){
    override fun doWork(): Result {
        // First sense

        // Performe action


        // Be happy :)/ Or sent a notification

        return Result.success();
    }
}