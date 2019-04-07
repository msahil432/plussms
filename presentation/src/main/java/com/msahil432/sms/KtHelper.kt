package com.msahil432.sms

import android.widget.ImageView
import com.moez.QKSMS.util.GlideApp
import com.moez.QKSMS.util.GlideRequest

/**
 * Created by msahil432
 **/

class KtHelper{
    companion object {
        public fun insertCompanyPhoto(photo: ImageView, address: String): Boolean{
            val add = address.toLowerCase()
            val glide = GlideApp.with(photo)
            val load = when{
                add.contains("paytm")->
                    glide.load(R.drawable.paytm)
                add.contains("axis")->
                    glide.load(R.drawable.axis)
                add.contains("sbi")->
                    glide.load(R.drawable.sbi2)
                add.contains("idfc")->
                    glide.load(R.drawable.idfc)
                add.contains("fchrge")->
                    glide.load(R.drawable.freecharge)
                add.contains("dbs") || add.contains("digib")->
                    glide.load(R.drawable.dbs)
                add.contains("icici")->
                    glide.load(R.drawable.icici)
                add.contains("kotak")->
                    glide.load(R.drawable.kotak)
                add.contains("phonpe")->
                    glide.load(R.drawable.phonepe)
                add.contains("grofrs")->
                    glide.load(R.drawable.grofers)
                add.contains("lenkrt")->
                    glide.load(R.drawable.lenskart)
                add.contains("amazon") || add=="51466" ->
                    glide.load(R.drawable.amazon)
                add.contains("zomato") ->
                    glide.load(R.drawable.zomato)
                add.contains("faasos") ->
                    glide.load(R.drawable.fasoos)
                add.contains("cnopls") ->
                    glide.load(R.drawable.cinepolis)
                else -> {
                    glide.load(R.drawable.paytm)
                    return false
                }
            }
            load.into(photo)
            return true
        }
    }
}