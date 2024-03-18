package com.app.phonebook.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.databinding.ItemFilterContactSourceBinding

class FilterContactSourcesAdapter(
    val activity: BaseActivity<*>,
    private val contactSources: List<ContactSource>,
    private val displayContactSources: ArrayList<String>
) : RecyclerView.Adapter<FilterContactSourcesAdapter.ViewHolder>() {

    private val selectedKeys = HashSet<Int>()

    init {
        contactSources.forEachIndexed { _, contactSource ->
            if (displayContactSources.contains(contactSource.name)) {
                selectedKeys.add(contactSource.hashCode())
            }

            if (contactSource.type == SMT_PRIVATE && displayContactSources.contains(SMT_PRIVATE)) {
                selectedKeys.add(contactSource.hashCode())
            }
        }
    }

    private fun toggleItemSelection(select: Boolean, contactSource: ContactSource, position: Int) {
        if (select) {
            selectedKeys.add(contactSource.hashCode())
        } else {
            selectedKeys.remove(contactSource.hashCode())
        }

        notifyItemChanged(position)
    }

    fun getSelectedContactSources() = contactSources.filter { selectedKeys.contains(it.hashCode()) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilterContactSourceBinding.inflate(activity.layoutInflater, parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactSource = contactSources[position]
        holder.bindView(contactSource)
    }

    override fun getItemCount() = contactSources.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(contactSource: ContactSource): View {
            val isSelected = selectedKeys.contains(contactSource.hashCode())
            ItemFilterContactSourceBinding.bind(itemView).apply {
                filterContactSourceCheckbox.isChecked = isSelected
                filterContactSourceCheckbox.setColors(
                    textColor = activity.getProperTextColor(),
                    accentColor = activity.getProperPrimaryColor()
                )
                val countText = if (contactSource.count >= 0) " (${contactSource.count})" else ""
                val displayName = "${contactSource.publicName}$countText"
                filterContactSourceCheckbox.text = displayName
                filterContactSourceHolder.setOnClickListener { viewClicked(!isSelected, contactSource) }

                return root
            }
        }

        private fun viewClicked(select: Boolean, contactSource: ContactSource) {
            toggleItemSelection(select, contactSource, adapterPosition)
        }
    }
}
