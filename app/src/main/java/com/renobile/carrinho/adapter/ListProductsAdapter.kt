package com.renobile.carrinho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ItemProductsBinding
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.setEmpty
import io.realm.RealmResults

class ListProductsAdapter(
    private val context: Context,
    private val listener: OnItemClickListener? = null
) :
    RecyclerView.Adapter<ListProductsAdapter.ViewHolder>(), RecyclerView.OnItemTouchListener {

    private var products: RealmResults<Product>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: RealmResults<Product>?) {
        products = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
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

    inner class ViewHolder(private val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(product: Product?) = with(binding) {
            if (product != null) {
                val price = product.price * product.quantity
                val plural = product.quantity.addPluralCharacter()

                tvName.text = product.name

                if (price > 0) {
                    tvTotal.text = price.formatPrice()

                    tvDetails.text = context.getString(
                        R.string.product_details,
                        product.quantity.formatQuantity(),
                        plural,
                        product.price.formatPrice()
                    )
                } else {
                    tvTotal.setEmpty()

                    tvDetails.text = context.getString(
                        R.string.product_details_no_price,
                        product.quantity.formatQuantity(),
                        plural,
                    )
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private var mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
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
