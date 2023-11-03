package com.renobile.carrinho.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kittinunf.fuel.httpGet
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.NotificationsAdapter
import com.renobile.carrinho.databinding.ActivityNotificationsBinding
import com.renobile.carrinho.util.API_MESSAGE
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.API_ROUTE_NOTIFICATIONS
import com.renobile.carrinho.util.API_SUCCESS
import com.renobile.carrinho.util.getBooleanVal
import com.renobile.carrinho.util.getJSONArrayVal
import com.renobile.carrinho.util.getStringVal
import com.renobile.carrinho.util.getValidJSONObject
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.show
import org.jetbrains.anko.displayMetrics
import org.json.JSONArray

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    private var notifications: JSONArray? = null
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadNotifications()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun loadNotifications() = with(binding) {
        progress.rlProgressLight.show()
        tvNotificationsEmpty.hide()

        API_ROUTE_NOTIFICATIONS.httpGet().responseString { request, response, result ->
            printFuelLog(request, response, result)

            var errorMessage = getString(R.string.error_connection)

            progress.rlProgressLight.hide()

            val (data, error) = result

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                errorMessage = apiObj.getStringVal(API_MESSAGE)

                if (apiObj.getBooleanVal(API_SUCCESS)) {
                    errorMessage = ""

                    notifications = apiObj.getJSONArrayVal(API_NOTIFICATIONS)
                }
            }

            if (errorMessage.isNotEmpty()) {
                tvNotificationsEmpty.show()
                tvNotificationsEmpty.text = errorMessage
            } else {
                renderNotifications()
            }
        }
    }

    private fun renderNotifications() = with(binding) {
        if (notifications == null || notifications!!.length() == 0) {
            tvNotificationsEmpty.setText(R.string.notifications_empty)
            tvNotificationsEmpty.show()
            rvNotifications.hide()
            return@with
        }

        tvNotificationsEmpty.hide()
        rvNotifications.show()

        rvNotifications.setHasFixedSize(true)

        val columns = if (displayMetrics.widthPixels > 1900) 2 else 1

        val layoutManager = GridLayoutManager(this@NotificationsActivity, columns)
        rvNotifications.layoutManager = layoutManager

        notificationsAdapter = NotificationsAdapter(this@NotificationsActivity)

        rvNotifications.adapter = notificationsAdapter

        val divider = DividerItemDecoration(this@NotificationsActivity, layoutManager.orientation)
        rvNotifications.addItemDecoration(divider)

        notificationsAdapter?.setData(notifications)
    }

}
