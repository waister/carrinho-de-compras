package com.renobile.carrinho.activity

import android.app.SearchManager
import android.content.Context
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartProductsAdapter
import com.renobile.carrinho.databinding.ActivityCartDetailsBinding
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.sendCart
import com.renobile.carrinho.util.show
import com.renobile.carrinho.util.toast
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class CartDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCartDetailsBinding

    private var realm: Realm = Realm.getDefaultInstance()
    private var cart: Cart? = null
    private var cartId: Long = 0
    private var products: RealmResults<Product>? = null
    private var historyAdapterCart: CartProductsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cartId = intent.getLongExtra(PARAM_CART_ID, 0)

        realm = Realm.getDefaultInstance()

        cart = realm.where(Cart::class.java).equalTo("id", cartId).findFirst()

        if (cart == null) {
            toast(R.string.error_cart_not_found)

            finish()
        } else {
            supportActionBar?.title = cart!!.name

            initViews()
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

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(terms: String): Boolean = doneSearch(terms)
        })
        return true
    }

    private fun getProducts(): RealmResults<Product>? {
        val query = realm.where(Product::class.java).equalTo("cartId", cartId)

        if (searchTerms.isNotEmpty())
            query?.contains("name", searchTerms, Case.INSENSITIVE)

        val productsResults = query?.findAll()

        return productsResults?.sort("id", Sort.DESCENDING)
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    override fun onClick(view: View?) {
        sendCart(products, cart!!.name)
    }

    private fun renderData() = with(binding) {
        products = getProducts()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.confirm_delete_cart)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    realm.executeTransaction {
                        products?.deleteAllFromRealm()

                        cart!!.deleteFromRealm()
                    }

                    toast(R.string.cart_deleted)

                    finish()
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
