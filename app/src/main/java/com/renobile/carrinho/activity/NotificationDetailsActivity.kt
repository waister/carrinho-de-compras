package com.renobile.carrinho.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R.*
import com.renobile.carrinho.util.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_notification_details.*
import kotlinx.android.synthetic.main.inc_progress_light.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class NotificationDetailsActivity : AppCompatActivity() {

    private var notificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_notification_details)

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

    private fun loadNotification() {
        rl_progress_light.visibility = View.VISIBLE

        val routeApi = API_ROUTE_NOTIFICATION + notificationId

        routeApi.httpGet().responseString { request, _, result ->
            Log.i("NotificationDetails", "Request: $request")

            if (ly_main == null) return@responseString

            var errorMessage = getString(string.error_connection)

            rl_progress_light.visibility = View.GONE

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

        tv_title.text = title
        tv_message.text = message
        tv_date.text = getString(string.label_received, date.formatDatetime())

        if (image.isValidUrl()) {
            Picasso.get()
                    .load(getThumbUrl(image))
                    .placeholder(drawable.ic_image_loading)
                    .error(drawable.ic_image_error)
                    .into(iv_image)
        } else {
            iv_image.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
