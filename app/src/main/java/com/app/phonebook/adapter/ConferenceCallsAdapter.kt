package com.app.phonebook.adapter

import android.annotation.SuppressLint
import android.telecom.Call
import android.view.Menu
import android.view.ViewGroup
import com.app.phonebook.R
import com.app.phonebook.base.extension.hasCapability
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.helpers.CallContactHelper.getCallContact
import com.app.phonebook.base.utils.LOWER_ALPHA
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseRecyclerViewAdapter
import com.app.phonebook.databinding.ItemConferenceCallBinding
import com.app.phonebook.helpers.SimpleContactsHelper
import com.app.phonebook.presentation.view.MyRecyclerView
import com.bumptech.glide.Glide

class ConferenceCallsAdapter(
    activity: BaseActivity<*>,
    recyclerView: MyRecyclerView,
    val data: ArrayList<Call>,
    itemClick: (Any) -> Unit
) : BaseRecyclerViewAdapter(activity, recyclerView, itemClick) {

    override fun actionItemPressed(id: Int) {}

    override fun getActionMenuId(): Int = 0

    override fun getIsItemSelectable(position: Int): Boolean = false

    override fun getItemCount(): Int = data.size

    override fun getItemKeyPosition(key: Int): Int = -1

    override fun getItemSelectionKey(position: Int): Int? = null

    override fun getSelectableItemCount(): Int = data.size

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun prepareActionMode(menu: Menu) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(
            ItemConferenceCallBinding.inflate(
                layoutInflater, parent, false
            ).root
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val call = data[position]
        holder.bindView(call, allowSingleClick = false, allowLongClick = false) { itemView, _ ->
            ItemConferenceCallBinding.bind(itemView).apply {
                getCallContact(itemView.context, call) { callContact ->
                    root.post {
                        itemConferenceCallName.text = callContact.name.ifEmpty {
                            itemView.context.getString(R.string.unknown_caller)
                        }
                        SimpleContactsHelper(activity).loadContactImage(
                            callContact.photoUri,
                            itemConferenceCallImage,
                            callContact.name,
                            activity.getDrawable(R.drawable.ic_person_vector)
                        )
                    }
                }

                val canSeparate =
                    call.hasCapability(Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE)
                val canDisconnect =
                    call.hasCapability(Call.Details.CAPABILITY_DISCONNECT_FROM_CONFERENCE)
                itemConferenceCallSplit.isEnabled = canSeparate
                itemConferenceCallSplit.alpha = if (canSeparate) 1.0f else LOWER_ALPHA
                itemConferenceCallSplit.setOnClickListener {
                    call.splitFromConference()
                    data.removeAt(position)
                    notifyItemRemoved(position)
                    if (data.size == 1) {
                        activity.finish()
                    }
                }

                itemConferenceCallSplit.setOnLongClickListener {
                    if (!it.contentDescription.isNullOrEmpty()) {
                        root.context.toast(it.contentDescription.toString())
                    }
                    true
                }

                itemConferenceCallEnd.isEnabled = canDisconnect
                itemConferenceCallEnd.alpha = if (canDisconnect) 1.0f else LOWER_ALPHA
                itemConferenceCallEnd.setOnClickListener {
                    call.disconnect()
                    data.removeAt(position)
                    notifyItemRemoved(position)
                    if (data.size == 1) {
                        activity.finish()
                    }
                }

                itemConferenceCallEnd.setOnLongClickListener {
                    if (!it.contentDescription.isNullOrEmpty()) {
                        root.context.toast(it.contentDescription.toString())
                    }
                    true
                }
            }
        }
        bindViewHolder(holder)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed && !activity.isFinishing) {
            ItemConferenceCallBinding.bind(holder.itemView).apply {
                Glide.with(activity).clear(itemConferenceCallImage)
            }
        }
    }
}
