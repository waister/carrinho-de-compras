package com.renobile.carrinho.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartsAdapter
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.PARAM_CART_ID
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_carts_history.*
import org.jetbrains.anko.intentFor

class CartsHistoryActivity : AppCompatActivity() {

    private var realm: Realm = Realm.getDefaultInstance()
    private var carts: RealmResults<Cart>? = null
    private var cartsAdapter: CartsAdapter? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carts_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()

        initViews()
    }

    private fun initViews() {
        rv_carts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        rv_carts.layoutManager = layoutManager

        val divider = DividerItemDecoration(this, layoutManager.orientation)
        rv_carts.addItemDecoration(divider)

        cartsAdapter = CartsAdapter(this)
        rv_carts.adapter = cartsAdapter

        rv_carts.addOnItemTouchListener(
            CartsAdapter(this, object : CartsAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val cart = carts!![position]

                    if (cart != null) {
                        startActivity(
                            intentFor<CartDetailsActivity>(
                                PARAM_CART_ID to cart.id
                            )
                        )
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

    private fun getCarts(terms: String = ""): RealmResults<Cart>? {
        var query = realm.where(Cart::class.java).greaterThan("dateClose", 0)

        if (terms.isNotEmpty()) {
            query = query?.contains("keywords", terms, Case.INSENSITIVE)
        }

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

    fun doneSearch(terms: String): Boolean {
        if (cartsAdapter != null && tv_empty != null) {
            renderData(terms)

            if (terms.isNotEmpty()) {
                return true
            }
        }

        return false
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun renderData(terms: String = "") {
        carts = getCarts(terms)

        if (carts!!.size == 0) {
            tv_empty.visibility = View.VISIBLE
        } else {
            tv_empty.visibility = View.GONE
        }

        cartsAdapter?.setData(carts)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!searchView!!.isIconified) {
            searchView!!.onActionViewCollapsed()
        } else {
            super.onBackPressed()
        }
    }
}
