package com.renobile.carrinho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ItemProductsBinding
import com.renobile.carrinho.domain.PurchaseList
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import io.realm.RealmResults

class ListsAdapter(
    private val context: Context,
    private val listener: OnItemClickListener? = null
) :
    RecyclerView.Adapter<ListsAdapter.ViewHolder>(), RecyclerView.OnItemTouchListener {

    private var carts: RealmResults<PurchaseList>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: RealmResults<PurchaseList>?) {
        carts = data
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
        holder.setData(carts!![position])
    }

    override fun getItemCount(): Int {
        if (carts == null) {
            return 0
        }
        return carts!!.size
    }

    inner class ViewHolder(private val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(cart: PurchaseList?) = with(binding) {
            if (cart != null) {
                tvName.text = cart.name
                tvTotal.text = cart.valueTotal.formatPrice()
                tvDetails.text = context.getString(
                    R.string.products_details,
                    cart.products,
                    cart.products.addPluralCharacter(),
                    cart.units.formatQuantity(),
                    cart.units.addPluralCharacter()
                )
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