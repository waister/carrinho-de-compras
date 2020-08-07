package com.renobile.carrinho.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.CartProductsAdapter
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.sendCart
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_cart_details.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class CartDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private var realm: Realm = Realm.getDefaultInstance()
    private var cart: Cart? = null
    private var cartId: Long = 0
    private var products: RealmResults<Product>? = null
    private var historyAdapterCart: CartProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_details)
        setSupportActionBar(toolbar)

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
    }

    private fun initViews() {
        fab.setOnClickListener(this)

        rv_products.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        rv_products.layoutManager = layoutManager

        val divider = DividerItemDecoration(this, layoutManager.orientation)
        rv_products.addItemDecoration(divider)

        historyAdapterCart = CartProductsAdapter(this)
        rv_products.adapter = historyAdapterCart

        renderData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cart_details, menu)
        return true
    }

    private fun getProducts(terms: String = ""): RealmResults<Product>? {
        var query = realm.where(Product::class.java)
                .equalTo("cartId", cartId)

        if (terms.isNotEmpty()) {
            query = query?.contains("name", terms, Case.INSENSITIVE)
        }

        val products = query?.findAll()

        return products?.sort("id", Sort.DESCENDING)
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    override fun onClick(view: View?) {
        sendCart(products, cart!!.name)
    }

    private fun renderData(terms: String = "") {
        products = getProducts(terms)

        var volumes = 0
        var total = 0.0

        if (products!!.size > 0) {
            products!!.forEach {
                volumes += it.quantity
                total += it.price * it.quantity
            }
        }

        tv_quantities.text = getString(R.string.products_details,
                products!!.size,
                if (products!!.size == 1) "" else "s",
                volumes,
                if (volumes == 1) "" else "s")
        tv_total.text = total.formatPrice()

        historyAdapterCart?.setData(products)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            alert(getString(R.string.confirm_delete_cart), getString(R.string.confirmation)) {
                positiveButton(R.string.confirm) {
                    realm.executeTransaction {
                        products?.deleteAllFromRealm()

                        cart!!.deleteFromRealm()
                    }

                    toast(R.string.cart_deleted)

                    finish()
                }
                negativeButton(R.string.cancel) {}
            }.show()
        } else {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
