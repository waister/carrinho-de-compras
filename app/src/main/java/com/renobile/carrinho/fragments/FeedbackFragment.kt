package com.renobile.carrinho.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.adapter.MessagesAdapter
import com.renobile.carrinho.util.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.find
import org.jetbrains.anko.okButton
import org.json.JSONArray

class FeedbackFragment : Fragment() {

    private lateinit var lyMain: RelativeLayout
    private lateinit var rvMessages: RecyclerView
    private lateinit var tvMessagesEmpty: TextView
    private lateinit var llLoading: LinearLayout
    private lateinit var etNewMessage: AppCompatEditText
    private lateinit var llRootProfile: LinearLayout
    private var messages: JSONArray? = null
    private var messagesChanged: Boolean = true
    private var apiAlreadyChecked: Boolean = false
    private var messagesAdapter: MessagesAdapter? = null
    private var rootProfile: View? = null
    private var menuUpdate: MenuItem? = null
    private var runnableCheckApi: Runnable = Runnable { checkApiMessages() }
    private var handlerCheckApi: Handler = Handler()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_feedback, container, false)

        lyMain = root.find(R.id.ly_main)
        rvMessages = root.find(R.id.rv_messages)
        tvMessagesEmpty = root.find(R.id.tv_messages_empty)
        llLoading = root.find(R.id.ll_loading)
        etNewMessage = root.find(R.id.et_new_message)
        llRootProfile = root.find(R.id.ll_root_profile)
        val btSendMessage = root.find<AppCompatImageButton>(R.id.ib_send_message)

        btSendMessage.setOnClickListener { submitMessage() }

        etNewMessage.setText(Hawk.get(PREF_COMMENTS, ""))

        messagesChanged = true
        apiAlreadyChecked = false
        messages = null

        checkApiMessages()
        checkApiWithDelay()

        rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            val length = messages?.length()

            if (bottom != oldBottom && length != null && length > 0) {
                rvMessages.scrollToPosition(length - 1)
            }
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_RELOAD && resultCode == Activity.RESULT_OK) {
            messagesChanged = true
            apiAlreadyChecked = false
            messages = null

            checkApiMessages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()

        Hawk.put(PREF_COMMENTS, etNewMessage.text.toString())

        handlerCheckApi.removeCallbacks(runnableCheckApi)
    }

    private fun checkApiWithDelay() {
        handlerCheckApi.removeCallbacks(runnableCheckApi)
        handlerCheckApi.postDelayed(runnableCheckApi, 5000)
    }

    @SuppressLint("InflateParams")
    private fun checkApiMessages() {
        if (!apiAlreadyChecked) {
            llLoading.visibility = View.VISIBLE
            tvMessagesEmpty.visibility = View.GONE
        }

        API_ROUTE_MESSAGES.httpGet().responseString { request, response, result ->
            printFuelLog(request, response, result)

            if (context == null) return@responseString

            checkApiWithDelay()

            var errorMessage = getString(R.string.error_connection)

            llLoading.visibility = View.GONE
            tvMessagesEmpty.visibility = View.GONE

            val (data, error) = result

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                errorMessage = apiObj.getStringVal(API_MESSAGE)

                if (apiObj.getBooleanVal(API_SUCCESS)) {
                    errorMessage = ""

                    var userName = apiObj.getStringVal(API_USER_NAME)
                    var userEmail = apiObj.getStringVal(API_USER_EMAIL)
                    val newMessages = apiObj.getJSONArrayVal(API_MESSAGES)

                    messagesChanged = messages?.length() != newMessages?.length()
                    messages = newMessages

                    if (userName.isEmpty())
                        userName = Hawk.get(PREF_USER_NAME, "")

                    if (userEmail.isEmpty())
                        userEmail = Hawk.get(PREF_USER_EMAIL, "")

                    if (userName.isEmpty() || userEmail.isEmpty()) {
                        showProfile()
                    } else {
                        Hawk.put(PREF_USER_NAME, userName)
                        Hawk.put(PREF_USER_EMAIL, userEmail)

                        menuUpdate?.isVisible = true
                    }
                }
            }


            if (errorMessage.isNotEmpty()) {
                if (!apiAlreadyChecked) {
                    tvMessagesEmpty.visibility = View.VISIBLE
                    tvMessagesEmpty.text = errorMessage
                }
            } else if (messagesChanged) {
                renderMessages()
            }

            apiAlreadyChecked = true
        }
    }

    private fun renderMessages() {
        if (messages == null || messages!!.length() == 0) {
            tvMessagesEmpty.setText(R.string.messages_empty)
            tvMessagesEmpty.visibility = View.VISIBLE
            rvMessages.visibility = View.GONE
            return
        }

        tvMessagesEmpty.visibility = View.GONE
        rvMessages.visibility = View.VISIBLE

        rvMessages.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(activity!!)
        rvMessages.layoutManager = layoutManager

        messagesAdapter = MessagesAdapter(activity!!)

        rvMessages.adapter = messagesAdapter

        messagesAdapter?.setData(messages)

        rvMessages.scrollToPosition(messages!!.length() - 1)
    }

    @SuppressLint("InflateParams")
    private fun showProfile() {
        val userName = Hawk.get(PREF_USER_NAME, "")
        val userEmail = Hawk.get(PREF_USER_EMAIL, "")

        val isUpdate = userName.isNotEmpty() || userEmail.isNotEmpty()

        rootProfile = layoutInflater.inflate(R.layout.fragment_feedback_profile, null)

        val etName = rootProfile!!.find<AppCompatEditText>(R.id.et_name)
        val etEmail = rootProfile!!.find<AppCompatEditText>(R.id.et_email)
        val btUpdate = rootProfile!!.find<AppCompatButton>(R.id.bt_update)
        val btCancel = rootProfile!!.find<AppCompatButton>(R.id.bt_cancel)

        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveProfile(etName, etEmail)
                true
            } else {
                false
            }
        }

        btUpdate.setOnClickListener {
            saveProfile(etName, etEmail)
        }

        btCancel.setOnClickListener {
            llRootProfile.visibility = View.GONE
            llRootProfile.removeAllViews()
        }

        etName.setText(userName)
        etEmail.setText(userEmail)

        if (isUpdate) {
            btUpdate.setText(R.string.save_changes)
            btCancel.visibility = View.VISIBLE
        }

        llRootProfile.addView(rootProfile)
        llRootProfile.visibility = View.VISIBLE
    }

    private fun saveProfile(etName: AppCompatEditText, etEmail: AppCompatEditText) {
        var error = false
        val name = etName.text.toString()
        val email = etEmail.text.toString()

        if (name.length < 3) {
            error = true
            etName.error = getString(R.string.error_name)
        }

        if (!email.isValidEmail()) {
            error = true
            etEmail.error = getString(R.string.error_email_invalid)
        }

        if (!error) {
            Hawk.put(PREF_USER_NAME, name)
            Hawk.put(PREF_USER_EMAIL, email)

            llRootProfile.visibility = View.GONE
            llRootProfile.removeAllViews()

            menuUpdate?.isVisible = true
        }
    }

    private fun submitMessage() {
        val comments = etNewMessage.text.toString()

        if (comments.isNotEmpty()) {

            etNewMessage.hideKeyboard()

            val title = getString(R.string.review_your_message)
            val message = getString(R.string.message_comments, comments)

            activity?.alert(message, title) {
                positiveButton(R.string.send_message) {
                    sendMessage(comments)
                }
                negativeButton(R.string.correct) {
                    etNewMessage.requestFocus()
                    etNewMessage.showKeyboard()
                }
            }?.show()

        }
    }

    private fun sendMessage(comments: String) {
        llLoading.visibility = View.VISIBLE

        val params = listOf(
                API_NAME to Hawk.get(PREF_USER_NAME, ""),
                API_EMAIL to Hawk.get(PREF_USER_EMAIL, ""),
                API_COMMENTS to comments)

        API_ROUTE_SEND_MESSAGE.httpPost(params).responseString { request, response, result ->
            printFuelLog(request, response, result)

            llLoading.visibility = View.GONE

            val (data, error) = result

            var success = false
            var message = ""

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                success = apiObj.getBooleanVal(API_SUCCESS)
                message = apiObj.getStringVal(API_MESSAGE)

                if (success) {
                    Hawk.delete(PREF_COMMENTS)

                    etNewMessage.setText("")

                    checkApiMessages()

                    activity?.alert(message, getString(R.string.success)) {
                        okButton {}
                    }?.show()

                    message = ""
                }
            }

            if (!success) {
                if (message.isEmpty())
                    message = getString(R.string.error_connection)

                activity?.alert(message, getString(R.string.ops)) {
                    okButton {}
                }?.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_profile, menu)

        menuUpdate = menu.findItem(R.id.action_update)
        menuUpdate?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_update) {
            showProfile()
        }
        return super.onOptionsItemSelected(item)
    }

}
