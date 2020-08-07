package com.renobile.carrinho.adapter

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.util.formatPrice
import io.realm.RealmResults
import org.jetbrains.anko.find

class CartsAdapter(private val context: Context,
                   private val listener: OnItemClickListener? = null) :
        RecyclerView.Adapter<CartsAdapter.ViewHolder>(), RecyclerView.OnItemTouchListener {

    private var carts: RealmResults<Cart>? = null

    fun setData(data: RealmResults<Cart>?) {
        carts = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_products, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(carts!![position])
    }

    override fun getItemCount(): Int {
        if (carts == null) {
            return 0
        }
        return carts!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvName = itemView.find<TextView>(R.id.tv_name)
        private var tvTotal = itemView.find<TextView>(R.id.tv_total)
        private var tvDetails = itemView.find<TextView>(R.id.tv_details)

        fun setData(cart: Cart?) {
            if (cart != null) {
                tvName.text = cart.name
                tvTotal.text = cart.valueTotal.formatPrice()
                tvDetails.text = context.getString(R.string.products_details,
                        cart.products,
                        if (cart.products == 1) "" else "s",
                        cart.units,
                        if (cart.units == 1) "" else "s")
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private var mGestureDetector: GestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean = true
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && listener != null && mGestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}