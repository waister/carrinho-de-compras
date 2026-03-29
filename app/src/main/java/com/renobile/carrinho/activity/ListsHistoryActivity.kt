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
import com.renobile.carrinho.adapter.ListsAdapter
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.databinding.ActivityListsHistoryBinding
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isVisible
import com.renobile.carrinho.util.show
import kotlinx.coroutines.launch

class ListsHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsHistoryBinding

    private lateinit var database: AppDatabase
    private var lists: List<PurchaseListEntity>? = null
    private var listsAdapter: ListsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsHistoryBinding.inflate(layoutInflater)
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
        rvLists.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this@ListsHistoryActivity)
        rvLists.layoutManager = layoutManager

        val divider = DividerItemDecoration(this@ListsHistoryActivity, layoutManager.orientation)
        rvLists.addItemDecoration(divider)

        listsAdapter = ListsAdapter(this@ListsHistoryActivity)
        rvLists.adapter = listsAdapter

        rvLists.addOnItemTouchListener(
            ListsAdapter(this@ListsHistoryActivity, object : ListsAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val list = lists?.getOrNull(position)

                    if (list != null) {
                        val intent = Intent(this@ListsHistoryActivity, ListDetailsActivity::class.java)
                        intent.putExtra(PARAM_LIST_ID, list.id)
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
        menuInflater.inflate(R.menu.lits_history_activity, menu)

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

    private suspend fun getListsList(): List<PurchaseListEntity> {
        val allLists = database.purchaseListDao().getAll().filter { it.dateClose > 0 }

        return if (searchTerms.isNotEmpty()) {
            allLists.filter {
                it.name.contains(searchTerms, ignoreCase = true)
            }
        } else {
            allLists
        }.sortedByDescending { it.id }
    }

    fun doneSearch(terms: String): Boolean = with(binding) {
        searchTerms = terms

        if (listsAdapter != null) {
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
            lists = getListsList()
            tvEmpty.isVisible(lists.isNullOrEmpty())
            listsAdapter?.setData(lists)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
