package com.renobile.carrinho.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.util.*
import org.jetbrains.anko.find
import org.json.JSONArray
import org.json.JSONObject

class MessagesAdapter(private val context: Context) :
        RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    private var messages: JSONArray? = null

    fun setData(data: JSONArray? = null) {
        messages = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (messages != null) {
            holder.setData(messages!!.getJSONObject(position))
        }
    }

    override fun getItemCount(): Int {
        if (messages != null) {
            return messages!!.length()
        }
        return 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var llLeft = itemView.find<LinearLayout>(R.id.ll_message_left)
        private var llRight = itemView.find<LinearLayout>(R.id.ll_message_right)

        private var tvLeftWho = itemView.find<TextView>(R.id.tv_left_who)
        private var tvRightWho = itemView.find<TextView>(R.id.tv_right_who)

        private var tvLeftText = itemView.find<TextView>(R.id.tv_left_text)
        private var tvLeftTime = itemView.find<TextView>(R.id.tv_left_time)

        private var tvRightText = itemView.find<TextView>(R.id.tv_right_text)
        private var tvRightTime = itemView.find<TextView>(R.id.tv_right_time)

        fun setData(messageObj: JSONObject) {
            val comments = messageObj.getStringVal(API_COMMENTS)
            val author = messageObj.getStringVal(API_AUTHOR)
            val timeAgo = messageObj.getStringVal(API_CREATED).formatDatetime()

            if (author == API_ADMIN) {
                llLeft.visibility = View.VISIBLE
                llRight.visibility = View.GONE

                tvLeftWho.setText(R.string.admin_write)
                tvLeftText.text = comments
                tvLeftTime.text = timeAgo
            } else {
                llLeft.visibility = View.GONE
                llRight.visibility = View.VISIBLE

                tvRightWho.setText(R.string.you_write)
                tvRightText.text = comments
                tvRightTime.text = timeAgo
            }
        }
    }
}