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
                           startActivity(intentFor<CartDetailsActivity>(
                                   PARAM_CART_ID to cart.id
                           ))
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
        menuInflater.inflate(R.menu.fragment_cart, menu)

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
            query = query?.contains("name", terms, Case.INSENSITIVE)
        }

        val products = query?.findAll()

        return products?.sort("id", Sort.DESCENDING)
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
