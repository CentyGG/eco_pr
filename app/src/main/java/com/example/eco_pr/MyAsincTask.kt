package com.example.eco_pr

import RecordController
import android.os.AsyncTask
import android.os.Handler
import android.util.Log

class MyAsincTask(var recordController: RecordController): AsyncTask<Void, Void, Void>() {
    override fun onPreExecute() {
        super.onPreExecute()
    }
    override fun doInBackground(vararg params: Void?): Void? {
        val decibelsArray: ArrayList<Double> = arrayListOf()
        var handler = Handler()
        handler.postDelayed({
            val volume = this.recordController.getVolume().toDouble()
            val decibels = Math.sqrt(volume) * 2
            decibelsArray.add(decibels)
            Log.i("TAG", "Added")},
            500)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}