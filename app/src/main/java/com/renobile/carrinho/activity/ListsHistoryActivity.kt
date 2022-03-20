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
import com.renobile.carrinho.adapter.ListsAdapter
import com.renobile.carrinho.domain.PurchaseList
import com.renobile.carrinho.util.PARAM_LIST_ID
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_lists_history.*
import org.jetbrains.anko.intentFor

class ListsHistoryActivity : AppCompatActivity() {

    private var realm: Realm = Realm.getDefaultInstance()
    private var lists: RealmResults<PurchaseList>? = null
    private var listsAdapter: ListsAdapter? = null
    private var searchView: SearchView? = null
    private var searchTerms: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()

        initViews()
    }

    private fun initViews() {
        rv_lists.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        rv_lists.layoutManager = layoutManager

        val divider = DividerItemDecoration(this, layoutManager.orientation)
        rv_lists.addItemDecoration(divider)

        listsAdapter = ListsAdapter(this)
        rv_lists.adapter = listsAdapter

        rv_lists.addOnItemTouchListener(
                ListsAdapter(this, object : ListsAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val list = lists!![position]

                        if (list != null) {
                            startActivity(intentFor<ListDetailsActivity>(
                                    PARAM_LIST_ID to list.id
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
        var query = realm.where(PurchaseList::class.java).greaterThan("dateClose", 0)

        if (searchTerms.isNotEmpty()) {
            query = query?.contains("name", searchTerms, Case.INSENSITIVE)
        }

        val products = query?.findAll()

        return products?.sort("id", Sort.DESCENDING)
    }

    fun doneSearch(terms: String): Boolean {
        searchTerms = terms

        if (listsAdapter != null && tv_empty != null) {
            renderData()

            if (searchTerms.isNotEmpty()) {
                return true
            }
        }

        return false
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun renderData() {
        lists = getLists()

        if (lists!!.size == 0) {
            tv_empty.visibility = View.VISIBLE
        } else {
            tv_empty.visibility = View.GONE
        }

        listsAdapter?.setData(lists)
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
