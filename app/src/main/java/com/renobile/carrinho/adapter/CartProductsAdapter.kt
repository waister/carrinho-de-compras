package com.renobile.carrinho.adapter

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.formatPrice
import io.realm.RealmResults
import org.jetbrains.anko.find

class CartProductsAdapter(private val context: Context,
                          private val listener: OnItemClickListener? = null) :
        RecyclerView.Adapter<CartProductsAdapter.ViewHolder>(), RecyclerView.OnItemTouchListener {

    private var products: RealmResults<Product>? = null

    fun setData(data: RealmResults<Product>?) {
        products = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(context)
                .inflate(R.layout.item_products, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(products!![position])
    }

    override fun getItemCount(): Int {
        if (products == null) {
            return 0
        }
        return products!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvName = itemView.find<TextView>(R.id.tv_name)
        private var tvTotal = itemView.find<TextView>(R.id.tv_total)
        private var tvDetails = itemView.find<TextView>(R.id.tv_details)

        fun setData(product: Product?) {
            if (product != null) {
                tvName.text = product.name
                tvTotal.text = (product.price * product.quantity).formatPrice()
                tvDetails.text = context.getString(R.string.product_details,
                        product.quantity,
                        if (product.quantity == 1) "" else "s",
                        product.price.formatPrice())
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
