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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartsAdapter
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.databinding.ActivityCartsHistoryBinding
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.PARAM_SEARCH_TERMS
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isVisible
import com.renobile.carrinho.util.show
import kotlinx.coroutines.launch

class CartsHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartsHistoryBinding

    private lateinit var database: AppDatabase
    private var carts: List<CartEntity>? = null
    private var cartsAdapter: CartsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = AppDatabase.getDatabase(this)

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
                    val cart = carts?.getOrNull(position)

                    if (cart != null) {
                        val intent = Intent(this@CartsHistoryActivity, CartDetailsActivity::class.java)
                        intent.putExtra(PARAM_CART_ID, cart.id)
                        intent.putExtra(PARAM_SEARCH_TERMS, searchTerms)
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

    private suspend fun getCartsList(): List<CartEntity> {
        val allCarts = database.cartDao().getAll().filter { it.dateClose > 0 }

        return if (searchTerms.isNotEmpty()) {
            allCarts.filter {
                it.name.contains(searchTerms, ignoreCase = true) ||
                        it.keywords.contains(searchTerms, ignoreCase = true)
            }
        } else {
            allCarts
        }.sortedByDescending { it.id }
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

    private fun renderData() = with(binding) {
        lifecycleScope.launch {
            updateMissingKeywords()

            carts = getCartsList()
            tvEmpty.isVisible(carts.isNullOrEmpty())
            cartsAdapter?.setData(carts)
        }
    }

    private suspend fun updateMissingKeywords() {
        val cartsWithoutKeywords = database.cartDao().getAll().filter { it.dateClose > 0 && it.keywords.isEmpty() }

        cartsWithoutKeywords.forEach { cart ->
            val products = database.productDao().getByCartId(cart.id)
            if (products.isNotEmpty()) {
                val keywords = products.joinToString(", ") { it.name }
                database.cartDao().insert(cart.copy(keywords = keywords))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
