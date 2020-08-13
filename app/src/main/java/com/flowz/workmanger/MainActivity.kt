package com.flowz.workmanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object{
        const val KEY_COUNT_VALUE = "key_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
//            setOneTimeWorkRequest()
              setPeriodicWorkRequest()
        }


    }

    private fun setOneTimeWorkRequest(){
        val workManager = WorkManager.getInstance(applicationContext)

        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()

        val constraints =  Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
            .build()

        val compressingWorker = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()

        val downloadingWorker = OneTimeWorkRequest.Builder(DownloadingWorker::class.java)
            .build()

        val parallelWorks = mutableListOf<OneTimeWorkRequest>()
        parallelWorks.add(downloadingWorker)
        parallelWorks.add(filteringRequest)


        workManager
            .beginWith(parallelWorks)
            .then(compressingWorker)
            .then(uploadRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView.text = it.state.name

                if (it.state.isFinished){

                    val data = it.outputData
                    val message = data.getString(UploadWorker.KEY_WORKER)
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setPeriodicWorkRequest(){

//android jetPack requires a minimum of 15 minutes for periodic request, so time interval parameter has to be 15 minutes or higher

        val periodicWorkRequest = PeriodicWorkRequest.Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }
}
