package com.renobile.carrinho.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.ListProductsAdapter
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.databinding.ActivityListDetailsBinding
import com.renobile.carrinho.databinding.ItemAddProductBinding
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.getDouble
import com.renobile.carrinho.util.maskMoney
import com.renobile.carrinho.util.sendList
import com.renobile.carrinho.util.setEmpty
import com.renobile.carrinho.util.toast
import kotlinx.coroutines.launch

class ListDetailsActivity : AppCompatActivity(), View.OnClickListener, ListProductsAdapter.OnItemClickListener {

    private lateinit var binding: ActivityListDetailsBinding

    private lateinit var database: AppDatabase
    private var list: PurchaseListEntity? = null
    private var listId: Long = 0
    private var products: List<ProductEntity>? = null
    private var productsAdapter: ListProductsAdapter? = null

    private var _addProductDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = AppDatabase.getDatabase(this)
        listId = intent.getLongExtra(PARAM_LIST_ID, 0)

        lifecycleScope.launch {
            list = database.purchaseListDao().getAll().find { it.id == listId }

            if (list == null) {
                toast(R.string.error_list_not_found)
                finish()
            } else {
                supportActionBar?.title = list!!.name
                initViews()
            }
        }

        setupInsets()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val appBar = binding.root.findViewById<AppBarLayout>(R.id.app_bar)
            appBar?.updatePadding(top = systemBars.top)
            view.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun initViews() = with(binding) {
        fab.setOnClickListener(this@ListDetailsActivity)

        rvProducts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this@ListDetailsActivity)
        rvProducts.layoutManager = layoutManager

        val divider = DividerItemDecoration(this@ListDetailsActivity, layoutManager.orientation)
        rvProducts.addItemDecoration(divider)

        productsAdapter = ListProductsAdapter(this@ListDetailsActivity, this@ListDetailsActivity)
        rvProducts.adapter = productsAdapter

        renderData()
    }

    override fun onItemClick(view: View, position: Int) {
        // Handle item click if needed
    }

    override fun onMoveToCartClick(product: ProductEntity) {
        lifecycleScope.launch {
            val activeCart = database.cartDao().getAll().find { it.dateClose == 0L }
            if (activeCart == null) {
                toast(R.string.create_cart_needed)
            } else {
                showMoveToCartDialog(product, activeCart.id)
            }
        }
    }

    private fun showMoveToCartDialog(product: ProductEntity, cartId: Long) {
        val bindingItem = ItemAddProductBinding.inflate(layoutInflater)

        bindingItem.etName.setText(product.name)
        bindingItem.etQuantity.setText(product.quantity.formatQuantity())
        bindingItem.etPrice.setText(product.price.formatPrice())

        bindingItem.etName.isEnabled = false
        bindingItem.tvAlert.text = getString(R.string.move_to_cart_notice)
        bindingItem.tvAlert.visibility = View.VISIBLE

        bindingItem.etPrice.maskMoney()
        bindingItem.etPrice.requestFocus()

        _addProductDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(bindingItem.root)
            .setTitle(R.string.move_to_cart)
            .setPositiveButton(R.string.confirm, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        _addProductDialog!!.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        _addProductDialog!!.setOnShowListener { dialog ->
            val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            bindingItem.etPrice.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    button.performClick()
                    true
                } else {
                    false
                }
            }

            button.setOnClickListener {
                val quantity = bindingItem.etQuantity.getDouble()
                val price = bindingItem.etPrice.getDouble()

                if (price <= 0) {
                    toast(R.string.error_price)
                } else {
                    lifecycleScope.launch {
                        val updatedProduct = product.copy(
                            cartId = cartId,
                            listId = 0L,
                            quantity = quantity,
                            price = price
                        )
                        database.productDao().insert(updatedProduct)
                        renderData()
                        toast(R.string.product_added)
                        dialog.dismiss()
                    }
                }
            }
        }

        _addProductDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_details, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _addProductDialog?.dismiss()
    }

    private suspend fun getProductsList(terms: String = ""): List<ProductEntity> {
        val allProducts = database.productDao().getByListId(listId)
        return if (terms.isNotEmpty()) {
            allProducts.filter { it.name.contains(terms, ignoreCase = true) }
        } else {
            allProducts
        }.sortedByDescending { it.id }
    }

    override fun onClick(view: View?) {
        sendList(products, list!!.name)
    }

    private fun renderData(terms: String = "") = with(binding) {
        lifecycleScope.launch {
            products = getProductsList(terms)

            var volumes = 0.0
            var total = 0.0

            if (products!!.isNotEmpty()) {
                products!!.forEach {
                    volumes += it.quantity
                    total += it.price * it.quantity
                }
            }

            tvQuantities.text = getString(
                R.string.products_details,
                products!!.size,
                products!!.size.addPluralCharacter(),
                volumes.formatQuantity(),
                volumes.addPluralCharacter()
            )
            tvTotal.text = total.formatPrice()

            productsAdapter?.setData(products)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.confirm_delete_list)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    lifecycleScope.launch {
                        products?.forEach { database.productDao().delete(it) }
                        list?.let { database.purchaseListDao().delete(it) }
                        
                        toast(R.string.list_deleted)
                        finish()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
