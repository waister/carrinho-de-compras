package com.renobile.carrinho.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.ListProductsAdapter
import com.renobile.carrinho.databinding.ActivityListDetailsBinding
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.domain.PurchaseList
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.sendList
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class ListDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityListDetailsBinding

    private var realm: Realm = Realm.getDefaultInstance()
    private var list: PurchaseList? = null
    private var listId: Long = 0
    private var products: RealmResults<Product>? = null
    private var productsAdapter: ListProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listId = intent.getLongExtra(PARAM_LIST_ID, 0)

        realm = Realm.getDefaultInstance()

        list = realm.where(PurchaseList::class.java).equalTo("id", listId).findFirst()

        if (list == null) {
            toast(R.string.error_list_not_found)

            finish()
        } else {
            supportActionBar?.title = list!!.name

            initViews()
        }
    }

    private fun initViews() = with(binding) {
        fab.setOnClickListener(this@ListDetailsActivity)

        rvProducts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this@ListDetailsActivity)
        rvProducts.layoutManager = layoutManager

        val divider = DividerItemDecoration(this@ListDetailsActivity, layoutManager.orientation)
        rvProducts.addItemDecoration(divider)

        productsAdapter = ListProductsAdapter(this@ListDetailsActivity)
        rvProducts.adapter = productsAdapter

        renderData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_details, menu)
        return true
    }

    private fun getProducts(terms: String = ""): RealmResults<Product>? {
        var query = realm.where(Product::class.java)
            .equalTo("listId", listId)

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
        sendList(products, list!!.name)
    }

    private fun renderData(terms: String = "") = with(binding) {
        products = getProducts(terms)

        var volumes = 0
        var total = 0.0

        if (products!!.size > 0) {
            products!!.forEach {
                volumes += it.quantity
                total += it.price * it.quantity
            }
        }

        tvQuantities.text = getString(
            R.string.products_details,
            products!!.size,
            if (products!!.size == 1) "" else "s",
            volumes,
            if (volumes == 1) "" else "s"
        )
        tvTotal.text = total.formatPrice()

        productsAdapter?.setData(products)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            alert(getString(R.string.confirm_delete_list), getString(R.string.confirmation)) {
                positiveButton(R.string.confirm) {
                    realm.executeTransaction {
                        products?.deleteAllFromRealm()

                        list!!.deleteFromRealm()
                    }

                    toast(R.string.list_deleted)

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
