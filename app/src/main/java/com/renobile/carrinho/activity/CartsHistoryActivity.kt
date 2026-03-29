package com.renobile.carrinho.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartsAdapter
import com.renobile.carrinho.databinding.ActivityCartsHistoryBinding
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isVisible
import com.renobile.carrinho.util.show
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class CartsHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartsHistoryBinding

    private var realm: Realm = Realm.getDefaultInstance()
    private var carts: RealmResults<Cart>? = null
    private var cartsAdapter: CartsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()

        initViews()

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.rlRoot) { view, insets ->
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
        rvCarts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this@CartsHistoryActivity)
        rvCarts.layoutManager = layoutManager

        val divider = DividerItemDecoration(this@CartsHistoryActivity, layoutManager.orientation)
        rvCarts.addItemDecoration(divider)

        cartsAdapter = CartsAdapter(this@CartsHistoryActivity)
        rvCarts.adapter = cartsAdapter

        rvCarts.addOnItemTouchListener(
            CartsAdapter(this@CartsHistoryActivity, object : CartsAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val cart = carts!![position]

                    if (cart != null) {
                        val intent = Intent(this@CartsHistoryActivity, CartDetailsActivity::class.java)
                        intent.putExtra(PARAM_CART_ID, cart.id)
                        startActivity(intent)
                    }
                }
            })
        )
    }

    override fun onResume() {
        super.onResume()

        renderData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cats_history_activity, menu)

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
        return true
    }

    private fun getCarts(): RealmResults<Cart>? {
        val query = realm.where(Cart::class.java).greaterThan("dateClose", 0)

        if (searchTerms.isNotEmpty())
            query?.contains("keywords", searchTerms, Case.INSENSITIVE)

        val carts = query?.findAll()

        carts?.forEach { cart ->
            if (cart.keywords.isEmpty()) {
                val products = realm.where(Product::class.java)
                    .equalTo("cartId", cart.id)
                    .findAll()

                var keywords = cart.name

                products.forEach { product ->
                    keywords += "," + product.name
                }

                if (keywords.isNotEmpty()) {
                    realm.executeTransaction {
                        cart.keywords = keywords.lowercase()

                        realm.copyToRealmOrUpdate(cart)
                    }
                }
            }
        }

        return carts?.sort("id", Sort.DESCENDING)
    }

    fun doneSearch(terms: String): Boolean = with(binding) {
        searchTerms = terms

        if (cartsAdapter != null) {
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

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun renderData() = with(binding) {
        carts = getCarts()

        tvEmpty.isVisible(carts!!.isEmpty())

        cartsAdapter?.setData(carts)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
