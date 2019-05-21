package com.msahil432.sms.common

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.msahil432.sms.util.GlideApp
import com.msahil432.sms.util.GlideRequest
import com.msahil432.sms.R
import java.util.regex.Pattern


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
                add.contains("pesave")-> "Pesave"
                add.contains("mobikw")-> "Mobikwik"

                //Banks
                add.contains("axis")-> "Axis Bank"
                add.contains("sbi")-> "State Bank of India"
                add.contains("idfc")-> "IDFC Bank"
                add.contains("dbs") || add.contains("digib")->
                    "DBS Bank"
                add.contains("icici")-> "ICICI Bank"
                add.contains("kotakb")-> "Kotak Bank"
                add.contains("hdfcbk") -> "HDFC Bank"

                // DTH
                add.contains("dishtv") -> "DishTV"

                // Mutual Funds
                add.contains("rmfund") -> "Reliance Mutual Funds"

                // Groceries
                add.contains("grofrs")-> "Grofers"

                // E-Retailers
                add.contains("amazon") || add=="51466" ->
                    "Amazon"
                add.contains("clubfa") || add.contains("lovecf") ->
                    "Club Factory"
                add.contains("pprfry") -> "Pepperfry"
                add.contains("wooplr") -> "Wooplr"
                add.contains("pmall") -> "Paytm Mall"
                add.contains("flpkrt") -> "Flipkart"

                // Glasses
                add.contains("lenkrt")-> "Lenskart"
                add.contains("coolws") -> "CoolWinks"

                // Food
                add.contains("zomato") -> "Zomato"
                add.contains("faasos") -> "Faasos"
                add.contains("fpanda") -> "Foodpanda"
                add.contains("mgcpin") -> "MagicPin"

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
                add.contains("lybrat") -> "Lybrate"
                add.contains("snapch") -> "SnapChat"
                add.contains("droom") -> "DROOM"
                add.contains("indane") -> "Indane Gas"
                add.contains("skllz") -> "Skillenza"
                add.contains("trai") -> "Telecomm Authority of India"
                add.contains("huawei") -> "Huawei"
                add.contains("gatefm") -> "GateForum"
                add.contains("xiaomi") -> "Xiaomi"
                add.contains("gohike") -> "Hike"

                else -> address
            }
        }

        public fun insertCompanyPhoto(photo: ImageView, address: String): Boolean{
            val name = getName(address)
            if(name!=address) {
                getLoadedGlide(address, photo.context).fitCenter().into(photo)
                return true
            }
            return false
        }

        public fun getLoadedGlide(address: String, context: Context): GlideRequest<Bitmap>{
            val name = getName(address)
            val glide = GlideApp.with(context).asBitmap()
            return when(name){
                // E-Wallets
                "Paytm" -> glide.load(R.drawable.paytm)
                "Freecharge" -> glide.load(R.drawable.freecharge)
                "PhonePe" -> glide.load(R.drawable.phonepe)
                "Pesave" -> glide.load(baseUrl +"pesave.com$imageSize")
                "Mobikwik" -> glide.load(R.drawable.mobikwik)

                //Banks
                "Axis Bank" -> glide.load(R.drawable.axis)
                "State Bank of India" -> glide.load(baseUrl +"onlinesbi.com$imageSize")
                "IDFC Bank" -> glide.load(baseUrl +"idfcbank.com$imageSize")
                "DBS Bank" -> glide.load(R.drawable.dbs)
                "ICICI Bank" -> glide.load(baseUrl +"icicibank.com$imageSize")
                "Kotak Bank" -> glide.load(R.drawable.kotak)
                "HDFC Bank" -> glide.load(baseUrl +"hadfcbank.com$imageSize")

                // Mutual Funds
                "Reliance Mutual Funds" -> glide.load(baseUrl +"reliancemutaul.com$imageSize")

                // DTH
                "DishTV" -> glide.load(R.drawable.dishtv)

                // Groceries
                "Grofers"  -> glide.load(baseUrl +"grofers.com$imageSize")

                // E-Retailers
                "Amazon" -> glide.load(baseUrl +"amazon.com$imageSize")
                "Club Factory"  -> glide.load(R.drawable.clubfactory)
                "Pepperfry" -> glide.load(baseUrl +"pepperfry.com$imageSize")
                "Wooplr" -> glide.load(baseUrl +"wooplr.com$imageSize")
                "Flipkart" -> glide.load(baseUrl +"flipkart.in$imageSize")

                // Glasses
                "Lenskart" -> glide.load(baseUrl +"lenskart.com$imageSize")
                "Coolwinks" -> glide.load(baseUrl +"coolwinks.com$imageSize")

                // Food
                "Zomato" -> glide.load(baseUrl +"zomato.com$imageSize")
                "Faasos" -> glide.load(baseUrl +"faasos.com$imageSize")
                "Foodpanda" -> glide.load(baseUrl +"foodpanda.in$imageSize")
                "MagicPin" -> glide.load(baseUrl +"magicpin.com$imageSize")

                // Ride Services
                "Uber" -> glide.load(baseUrl +"uber.com$imageSize")

                // Movies
                "CinePolis" -> glide.load(baseUrl +"cinepolis.com$imageSize")
                "Book My Show" -> glide.load(baseUrl +"bookmyshow.com$imageSize")

                // Delivery Services
                "Delhivery" -> glide.load(baseUrl +"delhivery.com$imageSize")

                // Mobile Operators
                "Vodafone" -> glide.load(baseUrl +"vodafone.in$imageSize")
                "Vodafone mPesa" -> glide.load(R.drawable.mpesa)
                "Vodafone Play" -> glide.load(baseUrl +"vodafone.com$imageSize")

                "Jio Money" -> glide.load(R.drawable.jiomoney)
                "Jio" -> glide.load(baseUrl +"jio.com$imageSize")

                "Airtel" -> glide.load(baseUrl +"airtel.com$imageSize")

                "MTNL" -> glide.load(baseUrl +"mtnldelhi.in$imageSize")

                // Misc.
                "Google" -> glide.load(baseUrl +"google.com$imageSize")

//                "Lybrate" -> glide.load(baseUrl +"lybrate.in$imageSize")

                "SnapChat" -> glide.load(baseUrl +"snapchat.com$imageSize")
                "Whatsapp" -> glide.load(baseUrl +"whatsapp.com$imageSize")
                "DROOM" -> glide.load(R.drawable.droom)
                "Indane Gas" -> glide.load(R.drawable.indane)
                "Skillenza" -> glide.load(baseUrl +"skillenza.com$imageSize")
                //Trai
                "Huawei" -> glide.load(baseUrl +"huawei.com$imageSize")
                "GateForum" -> glide.load(baseUrl +"gateforum.com$imageSize")
                "Xiaomi" -> glide.load(R.drawable.xiaomi)
                "Hike" -> glide.load(baseUrl +"hike.in$imageSize")

                else -> glide.load(address)
            }
        }

        public fun getOtp(text: String): String{
            val t = text.toLowerCase()
            if (!(t.contains("code") || t.contains("one time password")
                            || t.contains("otp")))
                return ""
            val p = Pattern.compile("\\d{4,8}")
            val m = p.matcher(t)
            if (!m.find())
                return ""
            try {
                return m.group(0)
            } catch (e: Exception) { }
            return ""
        }

    }
}