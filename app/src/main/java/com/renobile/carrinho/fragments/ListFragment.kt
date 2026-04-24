package com.renobile.carrinho.fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.ListsHistoryActivity
import com.renobile.carrinho.activity.MainActivity
import com.renobile.carrinho.adapter.ListProductsAdapter
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.databinding.FragmentListBinding
import com.renobile.carrinho.databinding.ItemAddListBinding
import com.renobile.carrinho.databinding.ItemAddProductBinding
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.createCartListName
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.getDouble
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isEmpty
import com.renobile.carrinho.util.longSnackbar
import com.renobile.carrinho.util.maskMoney
import com.renobile.carrinho.util.sendList
import com.renobile.carrinho.util.setEmpty
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.show
import com.renobile.carrinho.util.toast
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private var purchaseList: PurchaseListEntity? = null
    private var listId: Long = 0
    private var products: List<ProductEntity>? = null
    private var listProductsAdapter: ListProductsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""
    private var optionsMenu: Menu? = null
    private var _addListDialog: AlertDialog? = null
    private var _addProductDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        database = AppDatabase.getDatabase(requireContext())

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        setupInsets()
        initViews()

        return binding.root
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val appBar = binding.root.findViewById<AppBarLayout>(R.id.app_bar)
            appBar?.updatePadding(top = systemBars.top)
            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_list, menu)

        optionsMenu = menu
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
            R.id.action_new -> createNewList()
            R.id.action_send -> activity?.sendList(products, purchaseList!!.name)
            R.id.action_clear -> clearList()
            R.id.action_history -> {
                val intent = Intent(requireContext(), ListsHistoryActivity::class.java)
                startActivity(intent)
            }

            R.id.action_share_app -> activity?.shareApp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _addListDialog?.dismiss()
        _addProductDialog?.dismiss()
    }

    fun doneSearch(terms: String): Boolean = with(binding) {
        searchTerms = terms

        if (listProductsAdapter != null) {
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

    private fun createNewList() {
        if (activity == null) return

        if (_addListDialog == null) {
            val bindingItem = ItemAddListBinding.inflate(layoutInflater)

            bindingItem.etName.requestFocus()
            bindingItem.tvAlert.setText(R.string.create_list_notice)

            _addListDialog = AlertDialog.Builder(requireActivity())
                .setView(bindingItem.root)
                .setCancelable(false)
                .setTitle(R.string.create_list)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)
                .create()

            _addListDialog!!.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            _addListDialog!!.setOnShowListener { dialog ->
                val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

                bindingItem.etName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        button.performClick()
                        true
                    } else {
                        false
                    }
                }

                button.setOnClickListener {
                    var name = bindingItem.etName.text.toString()

                    if (name.isEmpty()) name = createCartListName()

                    lifecycleScope.launch {
                        if (products != null) {
                            var count = 0
                            var volumes = 0.0
                            var total = 0.0

                            products?.forEach {
                                count += 1
                                volumes += it.quantity
                                total += it.price * it.quantity
                            }

                            purchaseList?.let { currentList ->
                                val updatedList = currentList.copy(
                                    dateClose = System.currentTimeMillis(),
                                    products = count,
                                    units = volumes,
                                    valueTotal = total
                                )
                                database.purchaseListDao().insert(updatedList)
                            }
                        }

                        val lastList = database.purchaseListDao().getAll().firstOrNull()
                        val newId = (lastList?.id ?: 0L) + 1

                        val newList = PurchaseListEntity(
                            id = newId,
                            name = name,
                            dateOpen = System.currentTimeMillis(),
                            dateClose = 0L,
                            products = 0,
                            units = 0.0,
                            valueTotal = 0.0
                        )

                        database.purchaseListDao().insert(newList)

                        renderData()
                        dialog.dismiss()
                        binding.clRoot.longSnackbar(R.string.create_list_success)
                    }
                }
            }
        }
        _addListDialog?.show()
    }

    private fun clearList() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmation)
            .setMessage(R.string.confirm_delete_all)
            .setPositiveButton(R.string.confirm) { _, _ ->
                lifecycleScope.launch {
                    val productsToDelete = database.productDao().getByListId(listId)
                    productsToDelete.forEach { database.productDao().delete(it) }
                    renderData()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun initViews() = with(binding) {
        if (activity == null) return@with

        tvQuantities.hide()
        tvTotal.hide()

        btCreateList.setOnClickListener {
            createNewList()
        }

        fabAdd.setOnClickListener {
            if (purchaseList == null) {
                createNewList()
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

        listProductsAdapter = ListProductsAdapter(requireActivity(), object : ListProductsAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val product = products?.getOrNull(position)

                if (product != null) {
                    val options = resources.getStringArray(R.array.product_list_options)

                    AlertDialog.Builder(requireContext())
                        .setTitle(product.name)
                        .setItems(options) { _, i ->
                            when (i) {
                                0 -> {
                                    addOrEditProduct(product)
                                }

                                1 -> {
                                    moveToCart(product)
                                }

                                2 -> {
                                    changeQuantity(product, 1.0)
                                }

                                3 -> {
                                    changeQuantity(product, -1.0)
                                }

                                4 -> {
                                    deleteProduct(product)
                                }
                            }
                        }
                        .show()
                }
            }

            override fun onMoveToCartClick(product: ProductEntity) {
                moveToCart(product)
            }
        })
        rvProducts.adapter = listProductsAdapter

        renderData()
    }

    private suspend fun getProductsList(): List<ProductEntity> {
        val allProducts = database.productDao().getByListId(listId)
        return if (searchTerms.isNotEmpty()) {
            allProducts.filter { it.name.contains(searchTerms, ignoreCase = true) }
        } else {
            allProducts
        }.sortedByDescending { it.id }
    }

    private fun addOrEditProduct(product: ProductEntity? = null) {
        if (activity == null) return

        val bindingItem = ItemAddProductBinding.inflate(layoutInflater)

        bindingItem.etName.requestFocus()
        bindingItem.etPrice.maskMoney()

        var title = R.string.add_product
        var positive = R.string.add
        var negative = R.string.cancel
        var success = R.string.product_added

        if (product != null) {
            bindingItem.etName.setText(product.name)
            bindingItem.etQuantity.setText(product.quantity.formatQuantity())
            bindingItem.etPrice.setText(product.price.formatPrice())

            title = R.string.edit_product
            positive = R.string.save
            negative = R.string.discard
            success = R.string.product_edited
        }

        _addProductDialog = AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setView(bindingItem.root)
            .setTitle(title)
            .setPositiveButton(positive, null)
            .setNegativeButton(negative, null)
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

            lifecycleScope.launch {
                val names = database.productDao().getAllNames()
                val adapter = ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item, names)
                bindingItem.etName.setAdapter(adapter)
            }

            button.setOnClickListener {
                val name = bindingItem.etName.text.toString()
                val quantity = bindingItem.etQuantity.getDouble()
                val price = bindingItem.etPrice.getDouble()

                var error = 0

                when {
                    name.isEmpty() -> error = R.string.error_name
                    quantity.isEmpty() -> error = R.string.error_quantity
                }

                if (error != 0) {
                    requireContext().toast(error)
                } else {
                    lifecycleScope.launch {
                        val productId = product?.id ?: System.currentTimeMillis()

                        val item = ProductEntity(
                            id = productId,
                            cartId = 0L,
                            listId = listId,
                            name = name,
                            quantity = quantity,
                            price = price
                        )

                        database.productDao().insert(item)

                        bindingItem.etName.requestFocus()
                        renderData()

                        bindingItem.etName.setEmpty()
                        bindingItem.etQuantity.setText(R.string.one)
                        bindingItem.etPrice.setEmpty()

                        if (product != null) {
                            dialog.dismiss()
                            binding.clRoot.longSnackbar(success)
                        } else {
                            requireContext().toast(success)
                        }
                    }
                }
            }
        }

        _addProductDialog!!.show()
    }

    private fun changeQuantity(product: ProductEntity, delta: Double) = with(binding) {
        if (delta < 0 && (product.quantity + delta) < 0) {
            clRoot.longSnackbar(R.string.error_quantity_min)
        } else {
            lifecycleScope.launch {
                val updatedProduct = product.copy(quantity = product.quantity + delta)
                database.productDao().insert(updatedProduct)
                renderData()
                clRoot.longSnackbar(R.string.success_quantity)
            }
        }
    }

    private fun deleteProduct(product: ProductEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmation)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.confirm) { _, _ ->
                lifecycleScope.launch {
                    database.productDao().delete(product)
                    renderData()
                    binding.clRoot.longSnackbar(R.string.success_delete)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun moveToCart(product: ProductEntity) {
        lifecycleScope.launch {
            val activeCart = database.cartDao().getAll().find { it.dateClose == 0L }
            if (activeCart == null) {
                requireContext().toast(R.string.create_cart_needed)
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

        _addProductDialog = AlertDialog.Builder(requireActivity())
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
                    requireContext().toast(R.string.error_price)
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
                        requireContext().toast(R.string.product_added)
                        dialog.dismiss()
                    }
                }
            }
        }

        _addProductDialog!!.show()
    }

    private fun renderData() = with(binding) {
        lifecycleScope.launch {
            purchaseList = database.purchaseListDao().getAll().firstOrNull { it.dateClose == 0L }

            tvEmpty.hide()
            btCreateList.hide()

            val supportActionBar = (activity as AppCompatActivity).supportActionBar

            if (purchaseList == null) {
                supportActionBar?.setTitle(R.string.purchase_list)
                tvEmpty.setText(R.string.lists_empty)
                tvEmpty.show()
                btCreateList.show()
                tvQuantities.hide()
                tvTotal.hide()
                listProductsAdapter?.setData(null)
            } else {
                supportActionBar?.title = getString(R.string.label_list, purchaseList!!.name)

                optionsMenu?.findItem(R.id.action_send)?.isVisible = true
                optionsMenu?.findItem(R.id.action_clear)?.isVisible = true

                listId = purchaseList!!.id
                products = getProductsList()

                var volumes = 0.0
                var total = 0.0

                if (products!!.isNotEmpty()) {
                    products!!.forEach {
                        volumes += it.quantity
                        total += it.price * it.quantity
                    }
                } else {
                    tvEmpty.show()
                    tvEmpty.setText(R.string.products_empty)
                }

                tvQuantities.text = getString(
                    R.string.products_details,
                    products!!.size,
                    products!!.size.addPluralCharacter(),
                    volumes.formatQuantity(),
                    volumes.addPluralCharacter()
                )
                tvTotal.text = total.formatPrice()

                tvQuantities.show()
                tvTotal.show()

                listProductsAdapter?.setData(products)
            }
        }
    }

}
