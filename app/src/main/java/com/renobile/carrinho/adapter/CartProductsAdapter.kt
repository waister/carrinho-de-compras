package com.renobile.carrinho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.databinding.ItemProductsBinding
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity

class CartProductsAdapter(
    private val context: Context,
    private val listener: OnItemClickListener? = null
) :
    RecyclerView.Adapter<CartProductsAdapter.ViewHolder>() {

    private var products: List<ProductEntity>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<ProductEntity>?) {
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
        fun setData(product: ProductEntity?) = with(binding) {
            if (product != null) {
                tvName.text = product.name
                tvTotal.text = (product.price * product.quantity).formatPrice()
                tvDetails.text = context.getString(
                    R.string.product_details,
                    product.quantity.formatQuantity(),
                    product.quantity.addPluralCharacter(),
                    product.price.formatPrice()
                )

                root.setOnClickListener {
                    listener?.onItemClick(it, bindingAdapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}
