package com.renobile.carrinho.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R.drawable
import com.renobile.carrinho.R.string
import com.renobile.carrinho.databinding.ActivityNotificationDetailsBinding
import com.renobile.carrinho.util.API_BODY
import com.renobile.carrinho.util.API_DATE
import com.renobile.carrinho.util.API_IMAGE
import com.renobile.carrinho.util.API_LINK
import com.renobile.carrinho.util.API_MESSAGE
import com.renobile.carrinho.util.API_NOTIFICATION
import com.renobile.carrinho.util.API_ROUTE_NOTIFICATION
import com.renobile.carrinho.util.API_TITLE
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PREF_NOTIFICATION_JSON
import com.renobile.carrinho.util.formatDatetime
import com.renobile.carrinho.util.getJSONObjectVal
import com.renobile.carrinho.util.getStringVal
import com.renobile.carrinho.util.getThumbUrl
import com.renobile.carrinho.util.getValidJSONObject
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isValidUrl
import com.renobile.carrinho.util.show
import com.squareup.picasso.Picasso
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class NotificationDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailsBinding

    private var notificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemId = intent.getStringExtra(PARAM_ITEM_ID)

        val notificationObj = Hawk.get<JSONObject>(PREF_NOTIFICATION_JSON + itemId)

        if (notificationObj != null) {

            notificationId = itemId!!

            renderNotification(notificationObj)

        } else {

            loadNotification()

        }
    }

    private fun loadNotification() = with(binding) {
        progress.rlProgressLight.show()

        val routeApi = API_ROUTE_NOTIFICATION + notificationId

        routeApi.httpGet().responseString { request, _, result ->
            Log.i("NotificationDetails", "Request: $request")

            var errorMessage = getString(string.error_connection)

            progress.rlProgressLight.hide()

            val (data, error) = result

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                errorMessage = apiObj.getStringVal(API_MESSAGE)

                val notificationObj = apiObj.getJSONObjectVal(API_NOTIFICATION)

                if (notificationObj != null) {
                    errorMessage = ""

                    Hawk.put(PREF_NOTIFICATION_JSON + notificationId, notificationObj)

                    renderNotification(notificationObj)
                }
            }

            if (errorMessage.isNotEmpty()) {
                alert(errorMessage, getString(string.ops)) {
                    okButton { finish() }
                    onCancelled { finish() }
                }.show()
            }
        }
    }

    private fun renderNotification(notificationObj: JSONObject) {
        val title = notificationObj.getStringVal(API_TITLE)
        var message = notificationObj.getStringVal(API_BODY)
        val date = notificationObj.getStringVal(API_DATE)
        val image = notificationObj.getStringVal(API_IMAGE)
        val link = notificationObj.getStringVal(API_LINK)

        if (link.isNotEmpty())
            message += "\n\n" + getString(string.label_link, link)

        binding.apply {
            tvTitle.text = title
            tvMessage.text = message
            tvDate.text = getString(string.label_received, date.formatDatetime())

            if (image.isValidUrl()) {
                Picasso.get()
                    .load(getThumbUrl(image))
                    .placeholder(drawable.ic_image_loading)
                    .error(drawable.ic_image_error)
                    .into(ivImage)
            } else {
                ivImage.hide()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

}
