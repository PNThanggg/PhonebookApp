package com.app.phonebook.presentation.activities

import android.os.Bundle
import android.view.LayoutInflater
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.ActivityGroupContactsBinding
import com.app.phonebook.interfaces.RefreshContactsListener
import com.app.phonebook.interfaces.RemoveFromGroupListener

class GroupContactsActivity : BaseActivity<ActivityGroupContactsBinding>(), RemoveFromGroupListener, RefreshContactsListener {
    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityGroupContactsBinding {
        return ActivityGroupContactsBinding.inflate(inflater)
    }

    override fun refreshContacts(refreshTabsMask: Int) {

    }

    override fun contactClicked(contact: Contact) {
    }

    override fun removeFromGroup(contacts: ArrayList<Contact>) {
    }
}