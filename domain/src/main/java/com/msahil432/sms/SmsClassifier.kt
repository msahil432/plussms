package com.msahil432.sms

import android.util.Log
import com.msahil432.sms.model.ClassifierData
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

//        private val classifier = BayesClassifier<String, String>()

        private val adClassifier = BayesClassifier<String, String>()
        private val updateClassifier = BayesClassifier<String, String>()
        private val financeClassifier = BayesClassifier<String, String>()

        var count =0
        var ads =0
        var finance = 0
        var updates =0

        fun initializeClassifier(){
            adClassifier.reset()
            updateClassifier.reset()
            financeClassifier.reset()
            try {
                Realm.getDefaultInstance().where(ClassifierData::class.java)
                        .findAll()
                        .forEach { trainForEach(it.text, it.category) }
            }catch (e: Exception){
                val t3 = ClassifierDataSet().dataset
                t3.keys.forEach{
                        trainForEach(it, t3[it])
                }
            }
            Log.e("SmsClassifier", "Trained for : $count = f$finance + u$updates + a$ads")
        }

        private fun trainForEach(message: String, category: String?){
            if(category==null)
                return
            val text = message.split(" ")
            count++
            when (category) {
                CATEGORY_ADS -> {
                    adClassifier.learn(category, text)
                    updateClassifier.learn(NONE_CATEGORY, text)
                    financeClassifier.learn(NONE_CATEGORY, text)
                    ads++
                }
                CATEGORY_FINANCE -> {
                    adClassifier.learn(NONE_CATEGORY, text)
                    updateClassifier.learn(NONE_CATEGORY, text)
                    financeClassifier.learn(category, text)
                    finance++
                }
                CATEGORY_UPDATES -> {
                    adClassifier.learn(NONE_CATEGORY, text)
                    updateClassifier.learn(NONE_CATEGORY, text)
                    financeClassifier.learn(category, text)
                    updates++
                }
                CATEGORY_PERSONAL, CATEGORY_OTHERS -> {
                    adClassifier.learn(NONE_CATEGORY, text)
                    updateClassifier.learn(NONE_CATEGORY, text)
                    financeClassifier.learn(NONE_CATEGORY, text)
                }
            }
        }

//        fun initializeClassifier(){
//            var count =0
//            Realm.getDefaultInstance().where(ClassifierData::class.java)
//                    .findAll()
//                    .forEach {
//                        count++
//                        classifier.learn(it.category, it.text.split(" "))
//                    }
//            Log.e("SmsClassifier", "Trained for : $count")
//        }

        fun classify(text: String): String{
            val cleanedText = cleanPrivacy(text).split(" ")
            var cat = adClassifier.classify(cleanedText).takeIf { it!=null } ?: return NONE_CATEGORY
            if(cat.category== NONE_CATEGORY){
                cat = financeClassifier.classify(cleanedText)
                if(cat.category == NONE_CATEGORY){
                    cat = updateClassifier.classify(cleanedText)
                    if(cat.category == NONE_CATEGORY){
                        return CATEGORY_OTHERS
                    }
                }
            }
            Log.e("SmsClassifier", "$text Probablity for cat: ${cat.category} ${cat.probability}")
            return cat.category
        }

        fun cleanPrivacy(text: String): String {
            return text.replace("\\d".toRegex(), "").replace("\n", " ")
                    .replace(",", " ").replace("?", " ")
                    .replace("\\s\\s", " ")
        }
    }
}