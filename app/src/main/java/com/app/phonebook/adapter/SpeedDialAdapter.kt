package com.app.phonebook.adapter

import android.view.Menu
import android.view.ViewGroup
import com.app.phonebook.R
import com.app.phonebook.base.interfaces.RemoveSpeedDialListener
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseRecyclerViewAdapter
import com.app.phonebook.data.models.SpeedDial
import com.app.phonebook.databinding.ItemSpeedDialBinding
import com.app.phonebook.presentation.view.MyRecyclerView

class SpeedDialAdapter(
    activity: BaseActivity<*>,
    var speedDialValues: List<SpeedDial>,
    private val removeListener: RemoveSpeedDialListener,
    recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : BaseRecyclerViewAdapter(activity, recyclerView, itemClick) {
    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_delete_only

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> deleteSpeedDial()
        }
    }

    override fun getSelectableItemCount() = speedDialValues.size

    override fun getIsItemSelectable(position: Int) = speedDialValues[position].isValid()

    override fun getItemSelectionKey(position: Int) = speedDialValues.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = speedDialValues.indexOfFirst { it.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemSpeedDialBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val speedDial = speedDialValues[position]
        holder.bindView(speedDial, true, true) { itemView, layoutPosition ->
            val binding = ItemSpeedDialBinding.bind(itemView)
            setupView(binding, speedDial)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = speedDialValues.size

    private fun getSelectedItems() = speedDialValues.filter { selectedKeys.contains(it.hashCode()) } as ArrayList<SpeedDial>

    private fun deleteSpeedDial() {
        val ids = getSelectedItems().map { it.id }.toMutableList() as ArrayList<Int>
        removeListener.removeSpeedDial(ids)
        finishActMode()
    }

    private fun setupView(binding: ItemSpeedDialBinding, speedDial: SpeedDial) {
        binding.apply {
            var displayName = "${speedDial.id}. "
            displayName += if (speedDial.isValid()) speedDial.displayName else ""

            speedDialLabel.apply {
                text = displayName
                isSelected = selectedKeys.contains(speedDial.hashCode())
                setTextColor(textColor)
            }
        }
    }
}
