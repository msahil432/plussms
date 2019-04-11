package com.msahil432.sms

import android.util.Log
import com.moez.QKSMS.model.ClassifierData
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier
import io.realm.Realm

/**
 * Created by msahil432
 **/

class SmsClassifier{
    companion object {
        const val CATEGORY_PERSONAL = "Personal"
        const val CATEGORY_UPDATES = "Updates"
        const val CATEGORY_ADS = "Ads"
        const val CATEGORY_FINANCE = "Finance"
        const val CATEGORY_OTHERS = "Others"
        const val NONE_CATEGORY = "None"

        private val classifier = BayesClassifier<String, String>()
        init {
//            Executors.newSingleThreadExecutor().execute {
                classifier.memoryCapacity=500
                initializeClassifier()
                Log.e("SmsClassifier", "Initialized!")
//            }
        }

        fun initializeClassifier(){
            var count =0
            Realm.getDefaultInstance().where(ClassifierData::class.java)
                    .findAll()
                    .forEach {
                        count++
                        classifier.learn(it.category, it.text.split(" "))
                    }
            Log.e("SmsClassifier", "Trained for : $count")
        }

        fun classify(text: String): String{
            val cat = classifier.classify(cleanPrivacy(text).split(" "))
            Log.e("SmsClassifier", "Classifing: $text-- $cat")
            return if(cat==null) "NONE" else cat.category
        }

        fun cleanPrivacy(text: String): String {
            return text.replace("\\d".toRegex(), "").replace("\n", " ")
                    .replace(",", " ").replace("?", " ")
                    .replace("\\s\\s", " ")
        }
    }
}