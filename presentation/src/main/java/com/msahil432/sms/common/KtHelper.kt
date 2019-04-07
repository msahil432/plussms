package com.msahil432.sms.common

import android.widget.ImageView
import com.moez.QKSMS.util.GlideApp
import com.msahil432.sms.R

/**
 * Created by msahil432
 **/

class KtHelper{
    companion object {
        const val baseUrl = "https://logo.clearbit.com/"
        const val imageSize = "?size=64"
        public fun getName(address: String): String{
            val add = address.toLowerCase()
            return when{
                // E-Wallets
                add.contains("paytm") -> "Paytm"
                add.contains("fchrge")-> "Freecharge"
                add.contains("phonpe")-> "PhonePe"

                //Banks
                add.contains("axis")-> "Axis Bank"
                add.contains("sbi")-> "State Bank of India"
                add.contains("idfc")-> "IDFC Bank"
                add.contains("dbs") || add.contains("digib")->
                    "DBS Bank"
                add.contains("icici")-> "ICICI Bank"
                add.contains("kotak")-> "Kotak Bank"

                // Groceries
                add.contains("grofrs")-> "Grofers"

                // E-Retailers
                add.contains("amazon") || add=="51466" ->
                    "Amazon"
                add.contains("clubfa") || add.contains("lovecf") ->
                    "Club Factory"

                // Glasses
                add.contains("lenkrt")-> "Lenskart"

                // Food
                add.contains("zomato") -> "Zomato"
                add.contains("faasos") -> "Faasos"
                add.contains("fpanda") -> "Foodpanda"

                // Ride Services
                add.contains("uber") -> "Uber"

                // Movies
                add.contains("cnopls") -> "CinePolis"
                add.contains("bmshow") -> "Book My Show"

                // Delivery Services
                add.contains("dlhvry") -> "Delhivery"

                // Mobile Operators
                add.contains("vfcare") || add=="50404" || add=="611112" ||
                    add=="5454502" || add=="1925" || add=="611123"
                        || add=="Vodafone" -> "Vodafone"
                add.contains("mpesa") -> "Vodafone mPesa"
                add.contains("vfplay") -> "Vodafone Play"

                add.contains("jiopay") || add.contains("jiomny")
                    -> "Jio Money"
                add.contains("jio") -> "Jio"

                add.contains("airtel") || add=="121" -> "Airtel"

                add.contains("mtnl") || add=="651508" -> "MTNL"

                // Misc.
                add == "50360010" -> "Google"
                add.contains("lybrat") -> "Lybrate"
                add.contains("snapch") -> "SnapChat"
                add == "57575858" || add=="59039002"-> "Whatsapp"
                add.contains("droom") -> "DROOM"
                add.contains("indane") -> "Indane Gas"

                else -> address
            }
        }

        public fun insertCompanyPhoto(photo: ImageView, address: String): Boolean{
            val name = getName(address)
            val glide = GlideApp.with(photo)
            val load = when(name){
                // E-Wallets
                "Paytm" -> glide.load(R.drawable.paytm)
                "Freecharge" -> glide.load(R.drawable.freecharge)
                "PhonePe" -> glide.load(R.drawable.phonepe)

                //Banks
                "Axis Bank" -> glide.load(baseUrl +"axisbank.com$imageSize")
                "State Bank of India" -> glide.load(baseUrl +"onlinesbi.com$imageSize")
                "IDFC Bank" -> glide.load(baseUrl +"idfcbank.com$imageSize")
                "DBS Bank" -> glide.load(baseUrl +"dbs.com$imageSize")
                "ICICI Bank" -> glide.load(baseUrl +"icicibank.com$imageSize")
                "Kotak Bank" -> glide.load(baseUrl +"kotak.com$imageSize")

                // Groceries
                "Grofers"  -> glide.load(baseUrl +"grofers.com$imageSize")

                // E-Retailers
                "Amazon" -> glide.load(baseUrl +"amazon.com$imageSize")
                "Club Factory"  -> glide.load(R.drawable.clubfactory)

                // Glasses
                "Lenskart" -> glide.load(baseUrl +"lenskart.com$imageSize")

                // Food
                "Zomato" -> glide.load(baseUrl +"zomato.com$imageSize")
                "Faasos" -> glide.load(baseUrl +"faasos.com$imageSize")
                "Foodpanda" -> glide.load(baseUrl +"foodpanda.in$imageSize")

                // Ride Services
                "Uber" -> glide.load(baseUrl +"uber.com$imageSize")

                // Movies
                "CinePolis" -> glide.load(baseUrl +"cinepolis.com$imageSize")
                "Book My Show" -> glide.load(baseUrl +"bookmyshow.com$imageSize")

                // Delivery Services
                "Delhivery" -> glide.load(baseUrl +"delhivery.com$imageSize")

                // Mobile Operators
                "Vodafone" -> glide.load(baseUrl +"vodafone.com$imageSize")
                "Vodafone mPesa" -> glide.load(R.drawable.mpesa)
                "Vodafone Play" -> glide.load(baseUrl +"vodafone.com$imageSize")

                "Jio Money" -> glide.load(R.drawable.jiomoney)
                "Jio" -> glide.load(baseUrl +"jio.com$imageSize")

                "Airtel" -> glide.load(baseUrl +"airtel.com$imageSize")

                "MTNL" -> glide.load(baseUrl +"mtnldelhi.in$imageSize")

                // Misc.
                "Google" -> glide.load(baseUrl +"google.com$imageSize")
                "Lybrate" -> glide.load(baseUrl +"lybrate.in$imageSize")
                "SnapChat" -> glide.load(baseUrl +"snapchat.com$imageSize")
                "Whatsapp" -> glide.load(baseUrl +"whatsapp.com$imageSize")
                "DROOM" -> glide.load(R.drawable.droom)
                "Indane Gas" -> glide.load(R.drawable.indane)
                else -> {
                    glide.load(R.drawable.paytm)
                    return false
                }
            }
            load.fitCenter().into(photo)
            return true
        }
    }
}