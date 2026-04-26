package com.renobile.carrinho.activity

import android.app.SearchManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartProductsAdapter
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.databinding.ActivityCartDetailsBinding
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.PARAM_SEARCH_TERMS
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.sendCart
import com.renobile.carrinho.util.show
import com.renobile.carrinho.util.toast
import kotlinx.coroutines.launch

class CartDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCartDetailsBinding

    private lateinit var database: AppDatabase
    private var cart: CartEntity? = null
    private var cartId: Long = 0
    private var products: List<ProductEntity>? = null
    private var historyAdapterCart: CartProductsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = AppDatabase.getDatabase(this)
        cartId = intent.getLongExtra(PARAM_CART_ID, 0)
        searchTerms = intent.getStringExtra(PARAM_SEARCH_TERMS) ?: ""

        lifecycleScope.launch {
            cart = database.cartDao().getAll().find { it.id == cartId }

            if (cart == null) {
                toast(R.string.error_cart_not_found)
                finish()
            } else {
                supportActionBar?.title = cart!!.name
                initViews()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!searchView!!.isIconified)
                    searchView!!.onActionViewCollapsed()
                else
                    finish()
            }
        })

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
        fab.setOnClickListener(this@CartDetailsActivity)

        rvProducts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this@CartDetailsActivity)
        rvProducts.layoutManager = layoutManager

        val divider = DividerItemDecoration(this@CartDetailsActivity, layoutManager.orientation)
        rvProducts.addItemDecoration(divider)

        historyAdapterCart = CartProductsAdapter(this@CartDetailsActivity)
        rvProducts.adapter = historyAdapterCart

        renderData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cart_details, menu)

        searchView = menu.findItem(R.id.action_search)?.actionView as SearchView

        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(terms: String): Boolean = doneSearch(terms)
        })

        if (searchTerms.isNotEmpty()) {
            searchView?.isIconified = false
            searchView?.setQuery(searchTerms, true)
        }

        return true
    }

    private suspend fun getProductsList(): List<ProductEntity> {
        val allProducts = database.productDao().getByCartId(cartId)
        return if (searchTerms.isNotEmpty()) {
            allProducts.filter { it.name.contains(searchTerms, ignoreCase = true) }
        } else {
            allProducts
        }.sortedByDescending { it.id }
    }

    override fun onClick(view: View?) {
        sendCart(products, cart!!.name)
    }

    private fun renderData() = with(binding) {
        lifecycleScope.launch {
            products = getProductsList()

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

            historyAdapterCart?.setData(products)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.confirm_delete_cart)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    lifecycleScope.launch {
                        products?.forEach { database.productDao().delete(it) }
                        cart?.let { database.cartDao().delete(it) }

                        toast(R.string.cart_deleted)
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

    fun doneSearch(terms: String): Boolean = with(binding) {
        searchTerms = terms

        if (historyAdapterCart != null) {
            renderData()

            if (searchTerms.isNotEmpty()) {
                search.cvSearching.show()
                search.tvSearching.text = searchTerms

                return true
            }
        }

        search.cvSearching.hide()

        return false
    }

}
