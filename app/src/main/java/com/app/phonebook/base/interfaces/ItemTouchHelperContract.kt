package com.app.phonebook.base.interfaces

import com.app.phonebook.base.view.BaseRecyclerViewAdapter

interface ItemTouchHelperContract {
    fun onRowMoved(fromPosition: Int, toPosition: Int)

    fun onRowSelected(myViewHolder: BaseRecyclerViewAdapter.ViewHolder?)

    fun onRowClear(myViewHolder: BaseRecyclerViewAdapter.ViewHolder?)
}
