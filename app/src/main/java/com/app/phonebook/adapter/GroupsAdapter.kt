package com.app.phonebook.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.app.phonebook.R
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getTextSize
import com.app.phonebook.base.extension.groupsDB
import com.app.phonebook.base.extension.highlightTextPart
import com.app.phonebook.base.utils.TAB_GROUPS
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseRecyclerViewAdapter
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.ItemGroupBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.SimpleContactsHelper
import com.app.phonebook.interfaces.RefreshContactsListener
import com.app.phonebook.presentation.dialog.ConfirmationDialog
import com.app.phonebook.presentation.dialog.RenameGroupDialog
import com.app.phonebook.presentation.view.MyRecyclerView
import com.app.phonebook.presentation.view.RecyclerViewFastScroller

class GroupsAdapter(
    activity: BaseActivity<*>,
    var groups: ArrayList<Group>,
    recyclerView: MyRecyclerView,
    private val refreshListener: RefreshContactsListener?,
    itemClick: (Any) -> Unit
) : BaseRecyclerViewAdapter(activity, recyclerView, itemClick), RecyclerViewFastScroller.OnPopupTextUpdate {
    private var textToHighlight = ""
    private var showContactThumbnails = activity.config.showContactThumbnails

    var fontSize: Float = activity.getTextSize()

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_groups

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_rename).isVisible = isOneItemSelected()
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_rename -> renameGroup()
            R.id.cab_select_all -> selectAll()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = groups.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = groups.getOrNull(position)?.id?.toInt()

    override fun getItemKeyPosition(key: Int) = groups.indexOfFirst { it.id!!.toInt() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemGroupBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.bindView(group, allowSingleClick = true, allowLongClick = true) { itemView, _ ->
            setupView(itemView, group)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = groups.size

    private fun getItemWithKey(key: Int): Group? = groups.firstOrNull { it.id!!.toInt() == key }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: ArrayList<Group>, highlightText: String = "") {
        if (newItems.hashCode() != groups.hashCode()) {
            groups = newItems
            textToHighlight = highlightText
            notifyDataSetChanged()
            finishActMode()
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }
    }

    private fun renameGroup() {
        val group = getItemWithKey(selectedKeys.first()) ?: return
        RenameGroupDialog(activity, group) {
            finishActMode()
            refreshListener?.refreshContacts(TAB_GROUPS)
        }
    }

    private fun askConfirmDelete() {
        val itemsCnt = selectedKeys.size
        val firstItem = getSelectedItems().first()
        val items = if (itemsCnt == 1) {
            "\"${firstItem.title}\""
        } else {
            resources.getQuantityString(R.plurals.delete_groups, itemsCnt, itemsCnt)
        }

        val baseString = R.string.deletion_confirmation
        val question = String.format(resources.getString(baseString), items)

        ConfirmationDialog(activity, question) {
            ensureBackgroundThread {
                deleteGroups()
            }
        }
    }

    private fun deleteGroups() {
        if (selectedKeys.isEmpty()) {
            return
        }

        val groupsToRemove = groups.filter { selectedKeys.contains(it.id!!.toInt()) } as ArrayList<Group>
        val positions = getSelectedItemPositions()
        groupsToRemove.forEach {
            if (it.isPrivateSecretGroup()) {
                activity.groupsDB.deleteGroupId(it.id!!)
            } else {
                ContactsHelper(activity).deleteGroup(it.id!!)
            }
        }

        groups.removeAll(groupsToRemove.toSet())

        activity.runOnUiThread {
            if (groups.isEmpty()) {
                refreshListener?.refreshContacts(TAB_GROUPS)
                finishActMode()
            } else {
                removeSelectedItems(positions)
            }
        }
    }

    private fun getSelectedItems() = groups.filter { selectedKeys.contains(it.id?.toInt()) } as ArrayList<Group>

    private fun setupView(view: View, group: Group) {
        ItemGroupBinding.bind(view).apply {
            groupFrame.isSelected = selectedKeys.contains(group.id!!.toInt())
            val titleWithCnt = "${group.title} (${group.contactsCount})"
            val groupTitle = if (textToHighlight.isEmpty()) {
                titleWithCnt
            } else {
                titleWithCnt.highlightTextPart(textToHighlight, properPrimaryColor)
            }

            groupName.apply {
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                text = groupTitle
            }

            groupTmb.beVisibleIf(showContactThumbnails)

            if (showContactThumbnails) {
                groupTmb.setImageDrawable(SimpleContactsHelper(activity).getColoredGroupIcon(group.title))
            }
        }
    }

    override fun onChange(position: Int) = groups.getOrNull(position)?.getBubbleText() ?: ""
}
