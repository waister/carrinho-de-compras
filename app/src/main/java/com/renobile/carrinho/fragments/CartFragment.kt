package com.renobile.carrinho.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.CartsHistoryActivity
import com.renobile.carrinho.activity.MainActivity
import com.renobile.carrinho.adapter.CartProductsAdapter
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.util.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.*

class CartFragment : Fragment() {

    private var realm: Realm = Realm.getDefaultInstance()
    private var cart: Cart? = null
    private var cartId: Long = 0
    private var products: RealmResults<Product>? = null
    private var historyAdapterCart: CartProductsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""
    private var toolbar: Toolbar? = null
    private lateinit var clRoot: CoordinatorLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvQuantities: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btCreateCart: AppCompatButton
    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        toolbar = root.find(R.id.toolbar)
        clRoot = root.find(R.id.cl_root)
        tvTotal = root.find(R.id.tv_total)
        tvQuantities = root.find(R.id.tv_quantities)
        tvEmpty = root.find(R.id.tv_empty)
        btCreateCart = root.find(R.id.bt_create_cart)
        rvProducts = root.find(R.id.rv_products)
        fabAdd = root.find(R.id.fab_add)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        initViews()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_cart, menu)

        searchView = menu.findItem(R.id.action_search)?.actionView as SearchView

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(terms: String): Boolean = doneSearch(terms)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new -> createNewCart()
            R.id.action_send -> activity.sendCart(products, cart!!.name)
            R.id.action_clear -> clearCart()
            R.id.action_history -> activity?.startActivity(activity?.intentFor<CartsHistoryActivity>())
            R.id.action_share_app -> activity.shareApp()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNewCart() {
        if (activity == null) return

        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.item_add_cart, null)

        val tvAlert = view.find<TextView>(R.id.tv_alert)
        val etName = view.find<AppCompatEditText>(R.id.et_name)

        etName.requestFocus()

        val builder = AlertDialog.Builder(requireActivity())
                .setView(view)
                .setCancelable(false)
                .setTitle(R.string.create_cart)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)

        tvAlert.setText(R.string.create_cart_notice)

        val alert = builder.create()

        alert.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alert.setOnShowListener { dialog ->

            val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            etName.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    button.performClick()
                    true
                } else {
                    false
                }
            }

            button.setOnClickListener {
                val name = etName.text.toString()

                if (name.isEmpty()) {
                    activity?.toast(R.string.error_name)
                } else {
                    if (products != null) {
                        var count = 0
                        var volumes = 0
                        var total = 0.0

                        products?.forEach {
                            count += 1
                            volumes += it.quantity
                            total += it.price * it.quantity
                        }

                        val oldCart = realm.where(Cart::class.java).equalTo("id", cartId).findFirst()

                        if (oldCart != null) {
                            realm.executeTransaction {
                                oldCart.dateClose = System.currentTimeMillis()
                                oldCart.products = count
                                oldCart.units = volumes
                                oldCart.valueTotal = total

                                realm.copyToRealmOrUpdate(oldCart)
                            }
                        }
                    }

                    val maxId = realm.where(Cart::class.java).max("id")
                    val productId = if (maxId == null) 1 else maxId.toLong() + 1

                    val newCart = Cart()
                    newCart.id = productId
                    newCart.name = name
                    newCart.dateOpen = System.currentTimeMillis()

                    realm.executeTransaction {
                        realm.copyToRealmOrUpdate(newCart)
                    }

                    renderData()

                    dialog.dismiss()

                    clRoot.longSnackbar(R.string.create_cart_success)
                }
            }
        }
        alert.show()
    }

    private fun clearCart() {
        activity?.alert(R.string.confirm_delete_all, R.string.confirmation) {
            positiveButton(R.string.confirm) {
                realm.executeTransaction {
                    //                    realm.delete(Product::class.java)
                    realm.where(Product::class.java)
                            .equalTo("cartId", cartId)
                            .findAll()
                            .deleteAllFromRealm()

                    renderData()
                }
            }
            negativeButton(R.string.cancel) {}
        }?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun initViews() {
        if (activity == null) return

        tvQuantities.visibility = View.GONE
        tvTotal.visibility = View.GONE

        btCreateCart.setOnClickListener {
            createNewCart()
        }

        fabAdd.setOnClickListener {
            if (cart == null) {
                createNewCart()
            } else {
                (activity as MainActivity).showInterstitialAd()

                addOrEditProduct()
            }
        }

        rvProducts.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(activity)
        rvProducts.layoutManager = layoutManager

        val divider = DividerItemDecoration(activity, layoutManager.orientation)
        rvProducts.addItemDecoration(divider)

        historyAdapterCart = CartProductsAdapter(requireActivity())
        rvProducts.adapter = historyAdapterCart

        rvProducts.addOnItemTouchListener(
                CartProductsAdapter(requireActivity(), object : CartProductsAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val product = products!![position]

                        if (product != null) {
                            val options = resources.getStringArray(R.array.product_options)

                            activity?.selector(product.name, options.toList()) { _, i ->
                                when (i) {
                                    0 -> {
                                        addOrEditProduct(product)
                                    }
                                    1 -> {
                                        changeQuantity(product, +1)
                                    }
                                    2 -> {
                                        changeQuantity(product, -1)
                                    }
                                    3 -> {
                                        deleteProduct(product)
                                    }
                                }
                            }
                        }
                    }
                })
        )

        rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    if (fabAdd.isShown) fabAdd.hide()
                } else if (dy < 0) {
                    if (!fabAdd.isShown) fabAdd.show()
                }
            }
        })

        renderData()
    }

    private fun getProducts(): RealmResults<Product>? {
        var query = realm.where(Product::class.java)
                .equalTo("cartId", cartId)

        if (searchTerms.isNotEmpty()) {
            query = query?.contains("name", searchTerms, Case.INSENSITIVE)
        }

        val products = query?.findAll()

        return products?.sort("id", Sort.DESCENDING)
    }

    fun doneSearch(terms: String): Boolean {
        searchTerms = terms

        if (historyAdapterCart != null) {
            renderData()

            if (searchTerms.isNotEmpty()) {
                return true
            }
        }

        return false
    }


    private fun addOrEditProduct(product: Product? = null) {
        if (activity == null) return

        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.item_add_product, null)

        val etName = view.find<AutoCompleteTextView>(R.id.et_name)
        val etQuantity = view.find<AppCompatEditText>(R.id.et_quantity)
        val etPrice = view.find<AppCompatEditText>(R.id.et_price)

        etName.requestFocus()
        etPrice.maskMoney()

        var title = R.string.add_product
        var positive = R.string.add
        var negative = R.string.cancel
        var success = R.string.product_added

        if (product != null) {
            etName.setText(product.name)
            etQuantity.setText(product.quantity.toString())
            etPrice.setText(product.price.formatPrice())

            title = R.string.edit_product
            positive = R.string.save
            negative = R.string.discard
            success = R.string.product_edited
        }

        val alert = AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .create()
        alert.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alert.setOnShowListener { dialog ->

            val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            etPrice.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    button.performClick()
                    true
                } else {
                    false
                }
            }

            val products = realm.where(Product::class.java)
                    .sort("name", Sort.ASCENDING)
                    .findAll()

            if (products != null && products.size > 0) {
                val list = mutableListOf<String>()

                products.forEach {
                    if (!list.contains(it.name))
                        list.add(it.name)
                }

                val adapter = ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item, list)
                etName.setAdapter(adapter)
            }

            button.setOnClickListener {
                val name = etName.text.toString()
                val quantity = etQuantity.text.toString()
                val price = etPrice.text.toString().getPrice()

                var error = 0

                when {
                    name.isEmpty() -> error = R.string.error_name
                    quantity.isEmpty() -> error = R.string.error_quantity
                    price.isEmpty() -> error = R.string.error_price
                }

                if (error == 0 && quantity.getInt() < 1) {
                    error = R.string.error_quantity_min
                }

                if (error != 0) {
                    activity?.toast(error)
                } else {
                    val productId = if (product == null) {
                        val maxId = realm.where(Product::class.java).max("id")
                        if (maxId == null) 1 else maxId.toLong() + 1
                    } else {
                        product.id
                    }

                    val item = Product()
                    item.id = productId
                    item.cartId = cartId
                    item.name = name
                    item.quantity = quantity.getInt()
                    item.price = price.toDouble()

                    realm.executeTransaction {
                        realm.copyToRealmOrUpdate(item)
                    }

                    etName.requestFocus()

                    renderData()

                    etName.setText("")
                    etQuantity.setText("1")
                    etPrice.setText("")

                    if (product != null) {
                        dialog.dismiss()

                        clRoot.longSnackbar(success)
                    } else {
                        activity?.toast(success)
                    }
                }
            }
        }
        alert.show()
    }

    private fun changeQuantity(product: Product, quantity: Int) {
        if (quantity < 0 && product.quantity == 1) {
            clRoot.longSnackbar(R.string.error_quantity_min)
        } else {
            realm.executeTransaction {
                product.quantity += quantity

                realm.copyToRealmOrUpdate(product)
            }

            renderData()

            clRoot.longSnackbar(R.string.success_quantity)
        }
    }

    private fun deleteProduct(product: Product) {
        activity?.alert(getString(R.string.confirm_delete), getString(R.string.confirmation)) {
            positiveButton(R.string.confirm) {
                realm.executeTransaction {
                    product.deleteFromRealm()
                }

                renderData()

                clRoot.longSnackbar(R.string.success_delete)
            }
            negativeButton(R.string.cancel) {}
        }?.show()
    }

    private fun renderData() {
        if (realm.isClosed)
            realm = Realm.getDefaultInstance()

        cart = realm.where(Cart::class.java)
                .equalTo("dateClose", 0L)
                .findFirst()

        tvEmpty.visibility = View.GONE
        btCreateCart.visibility = View.GONE

        val supportActionBar = (activity as AppCompatActivity).supportActionBar

        if (cart == null) {

            supportActionBar?.setTitle(R.string.app_name)

            tvEmpty.setText(R.string.carts_empty)
            tvEmpty.visibility = View.VISIBLE
            btCreateCart.visibility = View.VISIBLE

        } else {

            supportActionBar?.title = getString(R.string.label_cart, cart!!.name)

            cartId = cart!!.id

            products = getProducts()

            val items = getProducts()
            var volumes = 0
            var total = 0.0

            if (items!!.size > 0) {
                items.forEach {
                    volumes += it.quantity
                    total += it.price * it.quantity
                }
            } else {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.setText(R.string.products_empty)
            }

            tvQuantities.text = getString(R.string.products_details,
                    items.size,
                    if (items.size == 1) "" else "s",
                    volumes,
                    if (volumes == 1) "" else "s")
            tvTotal.text = total.formatPrice()

            tvQuantities.visibility = View.VISIBLE
            tvTotal.visibility = View.VISIBLE

            historyAdapterCart?.setData(products)
        }
    }

}
