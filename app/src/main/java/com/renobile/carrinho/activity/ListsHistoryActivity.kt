package com.renobile.carrinho.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.ListsAdapter
import com.renobile.carrinho.databinding.ActivityListsHistoryBinding
import com.renobile.carrinho.domain.PurchaseList
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isVisible
import com.renobile.carrinho.util.show
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.intentFor

class ListsHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsHistoryBinding

    private var realm: Realm = Realm.getDefaultInstance()
    private var lists: RealmResults<PurchaseList>? = null
    private var listsAdapter: ListsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val list = lists!![position]

                    if (list != null) {
                        startActivity(
                            intentFor<ListDetailsActivity>(
                                PARAM_LIST_ID to list.id
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
        menuInflater.inflate(R.menu.fragment_list, menu)

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

    private fun getLists(): RealmResults<PurchaseList>? {
        val query = realm.where(PurchaseList::class.java).greaterThan("dateClose", 0)

        if (searchTerms.isNotEmpty())
            query?.contains("name", searchTerms, Case.INSENSITIVE)

        val products = query?.findAll()

        return products?.sort("id", Sort.DESCENDING)
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

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun renderData() = with(binding) {
        lists = getLists()

        tvEmpty.isVisible(lists!!.size == 0)

        listsAdapter?.setData(lists)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
