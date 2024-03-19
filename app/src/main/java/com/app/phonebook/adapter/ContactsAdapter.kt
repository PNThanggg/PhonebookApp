package com.app.phonebook.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.net.Uri
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.app.phonebook.R
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.areMultipleSIMsAvailable
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getTextSize
import com.app.phonebook.base.extension.highlightTextFromNumbers
import com.app.phonebook.base.extension.highlightTextPart
import com.app.phonebook.base.extension.launchSendSMSIntent
import com.app.phonebook.base.extension.setupViewBackground
import com.app.phonebook.base.extension.shortcutManager
import com.app.phonebook.base.extension.startContactDetailsIntent
import com.app.phonebook.base.helpers.ItemMoveCallback
import com.app.phonebook.base.interfaces.ItemTouchHelperContract
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.interfaces.StartReorderDragListener
import com.app.phonebook.base.utils.CONTACTS_GRID_MAX_COLUMNS_COUNT
import com.app.phonebook.base.utils.PERMISSION_CALL_PHONE
import com.app.phonebook.base.utils.PERMISSION_WRITE_CONTACTS
import com.app.phonebook.base.utils.VIEW_TYPE_GRID
import com.app.phonebook.base.utils.VIEW_TYPE_LIST
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseRecyclerViewAdapter
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.ItemContactWithoutNumberBinding
import com.app.phonebook.databinding.ItemContactWithoutNumberGridBinding
import com.app.phonebook.helpers.SimpleContactsHelper
import com.app.phonebook.presentation.dialog.ConfirmationDialog
import com.app.phonebook.presentation.view.MyRecyclerView
import com.bumptech.glide.Glide
import java.util.*

class ContactsAdapter(
    activity: BaseActivity<*>,
    var contacts: MutableList<Contact>,
    recyclerView: MyRecyclerView,
    highlightText: String = "",
    private val refreshItemsListener: RefreshItemsListener? = null,
    var viewType: Int = VIEW_TYPE_LIST,
    private val showDeleteButton: Boolean = true,
    private val enableDrag: Boolean = false,
    private val allowLongClick: Boolean = true,
    itemClick: (Any) -> Unit
) : BaseRecyclerViewAdapter(activity, recyclerView, itemClick),
    ItemTouchHelperContract, MyRecyclerView.MyZoomListener {
    private var textToHighlight = highlightText
    var fontSize: Float = activity.getTextSize()
    private var touchHelper: ItemTouchHelper? = null
    private var startReorderDragListener: StartReorderDragListener? = null
    var onDragEndListener: (() -> Unit)? = null
    var onSpanCountListener: (Int) -> Unit = {}

    init {
        setupDragListener(true)

        if (recyclerView.layoutManager is GridLayoutManager) {
            setupZoomListener(this)
        }

        if (enableDrag) {
            touchHelper = ItemTouchHelper(ItemMoveCallback(this, viewType == VIEW_TYPE_GRID))
            touchHelper?.attachToRecyclerView(recyclerView)

            startReorderDragListener = object : StartReorderDragListener {
                override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                    touchHelper?.startDrag(viewHolder)
                }
            }
        }
    }

    override fun getActionMenuId() = R.menu.cab_contacts

    override fun prepareActionMode(menu: Menu) {
        val hasMultipleSIMs = activity.areMultipleSIMsAvailable()
        val isOneItemSelected = isOneItemSelected()
        val selectedNumber = "tel:${getSelectedPhoneNumber()}"

        menu.apply {
            findItem(R.id.cab_call_sim_1).isVisible = hasMultipleSIMs && isOneItemSelected
            findItem(R.id.cab_call_sim_2).isVisible = hasMultipleSIMs && isOneItemSelected
            findItem(R.id.cab_remove_default_sim).isVisible =
                isOneItemSelected && (activity.config.getCustomSIM(selectedNumber) ?: "") != ""

            findItem(R.id.cab_delete).isVisible = showDeleteButton
            findItem(R.id.cab_create_shortcut).isVisible = isOneItemSelected
            findItem(R.id.cab_view_details).isVisible = isOneItemSelected
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_call_sim_1 -> callContact(true)
            R.id.cab_call_sim_2 -> callContact(false)
            R.id.cab_remove_default_sim -> removeDefaultSIM()
            R.id.cab_delete -> askConfirmDelete()
            R.id.cab_send_sms -> sendSMS()
            R.id.cab_create_shortcut -> tryCreateShortcut()
            R.id.cab_select_all -> selectAll()
        }
    }

    override fun getSelectableItemCount() = contacts.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = contacts.getOrNull(position)?.rawId

    override fun getItemKeyPosition(key: Int) = contacts.indexOfFirst { it.rawId == key }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActionModeCreated() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActionModeDestroyed() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Binding.getByItemViewType(viewType).inflate(layoutInflater, parent, false)
        return createViewHolder(binding.root)
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bindView(contact, true, allowLongClick) { itemView, _ ->
            val viewType = getItemViewType(position)
            setupView(Binding.getByItemViewType(viewType).bind(itemView), contact, holder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = contacts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<Contact>, highlightText: String = "") {
        if (newItems.hashCode() != contacts.hashCode()) {
            contacts = ArrayList(newItems)
            textToHighlight = highlightText
            notifyDataSetChanged()
            finishActMode()
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingPermission")
    private fun callContact(useSimOne: Boolean) {
        val number = getSelectedPhoneNumber() ?: return
        activity.callContactWithSim(number, useSimOne)
    }

    private fun removeDefaultSIM() {
        val phoneNumber = getSelectedPhoneNumber() ?: return
        activity.config.removeCustomSIM("tel:$phoneNumber")
        finishActMode()
    }

    private fun sendSMS() {
        val numbers = ArrayList<String>()
        getSelectedItems().map { simpleContact ->
            val contactNumbers = simpleContact.phoneNumbers
            val primaryNumber = contactNumbers.firstOrNull { it.isPrimary }
            val normalizedNumber =
                primaryNumber?.normalizedNumber ?: contactNumbers.firstOrNull()?.normalizedNumber

            if (normalizedNumber != null) {
                numbers.add(normalizedNumber)
            }
        }

        val recipient = TextUtils.join(";", numbers)
        activity.launchSendSMSIntent(recipient)
    }

    private fun viewContactDetails() {
        val contact = getSelectedItems().firstOrNull() ?: return
        activity.startContactDetailsIntent(contact)
    }

    private fun askConfirmDelete() {
        val itemsCnt = selectedKeys.size
        val firstItem = getSelectedItems().firstOrNull() ?: return
        val items = if (itemsCnt == 1) {
            "\"${firstItem.getNameToDisplay()}\""
        } else {
            resources.getQuantityString(R.plurals.delete_contacts, itemsCnt, itemsCnt)
        }

        val baseString = R.string.deletion_confirmation
        val question = String.format(resources.getString(baseString), items)

        ConfirmationDialog(activity, question) {
            activity.handlePermission(PERMISSION_WRITE_CONTACTS) {
                deleteContacts()
            }
        }
    }

    private fun deleteContacts() {
        if (selectedKeys.isEmpty()) {
            return
        }

        val contactsToRemove = getSelectedItems()
        val positions = getSelectedItemPositions()
        contacts.removeAll(contactsToRemove)
        val idsToRemove = contactsToRemove.map { it.rawId }.toMutableList() as ArrayList<Int>

        SimpleContactsHelper(activity).deleteContactRawIDs(idsToRemove) {
            activity.runOnUiThread {
                if (contacts.isEmpty()) {
                    refreshItemsListener?.refreshItems()
                    finishActMode()
                } else {
                    removeSelectedItems(positions)
                }
            }
        }
    }

    private fun getSelectedItems() =
        contacts.filter { selectedKeys.contains(it.rawId) } as ArrayList<Contact>

    private fun getSelectedPhoneNumber(): String? {
        return getSelectedItems().firstOrNull()?.getPrimaryNumber()
    }

    private fun tryCreateShortcut() {
        createShortcut()

//        if (activity.isOrWasThankYouInstalled()) {
//            createShortcut()
//        } else {
//            FeatureLockedDialog(activity) { }
//        }
    }

    @SuppressLint("NewApi")
    private fun createShortcut() {
        val contact = contacts.firstOrNull { selectedKeys.contains(it.rawId) } ?: return
        val manager = activity.shortcutManager
        if (manager.isRequestPinShortcutSupported) {
            SimpleContactsHelper(activity).getShortcutImage(
                contact.photoUri,
                contact.getNameToDisplay()
            ) { image ->
                activity.runOnUiThread {
                    activity.handlePermission(PERMISSION_CALL_PHONE) { hasPermission ->
                        val action = if (hasPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
                        val intent = Intent(action).apply {
                            data = Uri.fromParts("tel", getSelectedPhoneNumber(), null)
                        }

                        val shortcut = ShortcutInfo.Builder(activity, contact.hashCode().toString())
                            .setShortLabel(contact.getNameToDisplay())
                            .setIcon(Icon.createWithBitmap(image))
                            .setIntent(intent)
                            .build()

                        manager.requestPinShortcut(shortcut, null)
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed && !activity.isFinishing) {
            Binding.getByItemViewType(holder.itemViewType).bind(holder.itemView).apply {
                Glide.with(activity).clear(itemContactImage)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView(binding: ItemViewBinding, contact: Contact, holder: ViewHolder) {
        binding.apply {
            root.setupViewBackground(activity)
            itemContactFrame.isSelected = selectedKeys.contains(contact.rawId)
            itemContactName.apply {
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

                val name = contact.getNameToDisplay()
                text = if (textToHighlight.isEmpty()) name else {
                    if (name.contains(textToHighlight, true)) {
                        name.highlightTextPart(textToHighlight, properPrimaryColor)
                    } else {
                        name.highlightTextFromNumbers(textToHighlight, properPrimaryColor)
                    }
                }
            }

            if (enableDrag && textToHighlight.isEmpty()) {
                dragHandleIcon.apply {
                    beVisibleIf(selectedKeys.isNotEmpty())
                    applyColorFilter(textColor)
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            startReorderDragListener?.requestDrag(holder)
                        }
                        false
                    }
                }
            } else {
                dragHandleIcon.apply {
                    beGone()
                    setOnTouchListener(null)
                }
            }

            if (!activity.isDestroyed) {
                SimpleContactsHelper(root.context).loadContactImage(
                    contact.photoUri,
                    itemContactImage,
                    contact.getNameToDisplay()
                )
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        activity.config.isCustomOrderSelected = true

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(contacts, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(contacts, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {}

    override fun onRowClear(myViewHolder: ViewHolder?) {
        onDragEndListener?.invoke()
    }

    override fun zoomIn() {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val currentSpanCount = layoutManager.spanCount
            val newSpanCount = (currentSpanCount - 1).coerceIn(1, CONTACTS_GRID_MAX_COLUMNS_COUNT)
            layoutManager.spanCount = newSpanCount
            recyclerView.requestLayout()
            onSpanCountListener(newSpanCount)
        }
    }

    override fun zoomOut() {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val currentSpanCount = layoutManager.spanCount
            val newSpanCount = (currentSpanCount + 1).coerceIn(1, CONTACTS_GRID_MAX_COLUMNS_COUNT)
            layoutManager.spanCount = newSpanCount
            recyclerView.requestLayout()
            onSpanCountListener(newSpanCount)
        }
    }

    private sealed interface Binding {
        companion object {
            fun getByItemViewType(viewType: Int): Binding {
                return when (viewType) {
                    VIEW_TYPE_GRID -> ItemContactGrid
                    else -> ItemContact
                }
            }
        }

        fun inflate(
            layoutInflater: LayoutInflater,
            viewGroup: ViewGroup,
            attachToRoot: Boolean
        ): ItemViewBinding

        fun bind(view: View): ItemViewBinding

        data object ItemContactGrid : Binding {
            override fun inflate(
                layoutInflater: LayoutInflater,
                viewGroup: ViewGroup,
                attachToRoot: Boolean
            ): ItemViewBinding {
                return ItemContactGridBindingAdapter(
                    ItemContactWithoutNumberGridBinding.inflate(
                        layoutInflater,
                        viewGroup,
                        attachToRoot
                    )
                )
            }

            override fun bind(view: View): ItemViewBinding {
                return ItemContactGridBindingAdapter(ItemContactWithoutNumberGridBinding.bind(view))
            }
        }

        data object ItemContact : Binding {
            override fun inflate(
                layoutInflater: LayoutInflater,
                viewGroup: ViewGroup,
                attachToRoot: Boolean
            ): ItemViewBinding {
                return ItemContactBindingAdapter(
                    ItemContactWithoutNumberBinding.inflate(
                        layoutInflater,
                        viewGroup,
                        attachToRoot
                    )
                )
            }

            override fun bind(view: View): ItemViewBinding {
                return ItemContactBindingAdapter(ItemContactWithoutNumberBinding.bind(view))
            }
        }
    }

    private interface ItemViewBinding : ViewBinding {
        val itemContactName: TextView
        val itemContactImage: ImageView
        val itemContactFrame: ConstraintLayout
        val dragHandleIcon: ImageView
    }

    private class ItemContactGridBindingAdapter(
        val binding: ItemContactWithoutNumberGridBinding
    ) : ItemViewBinding {
        override val itemContactName = binding.itemContactName
        override val itemContactImage = binding.itemContactImage
        override val itemContactFrame = binding.itemContactFrame
        override val dragHandleIcon = binding.dragHandleIcon

        override fun getRoot(): View = binding.root
    }

    private class ItemContactBindingAdapter(
        val binding: ItemContactWithoutNumberBinding
    ) : ItemViewBinding {
        override val itemContactName = binding.itemContactName
        override val itemContactImage = binding.itemContactImage
        override val itemContactFrame = binding.itemContactFrame
        override val dragHandleIcon = binding.dragHandleIcon

        override fun getRoot(): View = binding.root
    }
}
