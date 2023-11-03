package com.renobile.carrinho.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.activity.NotificationDetailsActivity
import com.renobile.carrinho.databinding.ItemNotificationBinding
import com.renobile.carrinho.util.*
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject

class NotificationsAdapter(
    private val activity: Activity
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private var itemsArr: JSONArray? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: JSONArray?) {
        if (data != null) {
            itemsArr = data
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (itemsArr != null) {
            holder.setData(itemsArr!!.get(position) as JSONObject)
        }
    }

    override fun getItemCount(): Int {
        try {
            if (itemsArr != null) {
                return itemsArr!!.length()
            }
        } catch (_: IllegalStateException) {
        }
        return 0
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(itemObj: JSONObject) = with(binding) {
            val title = itemObj.getStringVal(API_TITLE)
            val body = itemObj.getStringVal(API_BODY)
            val date = itemObj.getStringVal(API_DATE)

            tvTitle.text = title
            tvMessage.text = body
            tvDate.text = date.formatDate()

            itemView.setOnClickListener {
                activity.startActivity(
                    activity.intentFor<NotificationDetailsActivity>(
                        PARAM_ITEM_ID to itemObj.getStringVal(API_ID)
                    )
                )
            }
        }
    }

}
