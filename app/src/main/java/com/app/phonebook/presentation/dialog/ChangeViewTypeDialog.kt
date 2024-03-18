package com.app.phonebook.presentation.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.phonebook.R
import com.app.phonebook.base.compose.alert_dialog.AlertDialogState
import com.app.phonebook.base.compose.alert_dialog.DialogSurface
import com.app.phonebook.base.compose.alert_dialog.rememberAlertDialogState
import com.app.phonebook.base.compose.components.RadioGroupDialogComponent
import com.app.phonebook.base.compose.extensions.MyDevices
import com.app.phonebook.base.compose.theme.AppThemeSurface
import com.app.phonebook.base.compose.theme.SimpleTheme
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.utils.VIEW_TYPE_GRID
import com.app.phonebook.base.utils.VIEW_TYPE_LIST
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.DialogChangeViewTypeBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class ChangeViewTypeDialog(val activity: BaseActivity<*>, val callback: () -> Unit) {
    private var view: DialogChangeViewTypeBinding
    private var config = activity.baseConfig

    init {
        view = DialogChangeViewTypeBinding.inflate(activity.layoutInflater, null, false).apply {
            val viewToCheck = when (config.viewType) {
                VIEW_TYPE_GRID -> changeViewTypeDialogRadioGrid.id
                else -> changeViewTypeDialogRadioList.id
            }

            changeViewTypeDialogRadio.check(viewToCheck)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogConfirmed() {
        val viewType = if (view.changeViewTypeDialogRadioGrid.isChecked) {
            VIEW_TYPE_GRID
        } else {
            VIEW_TYPE_LIST
        }
        config.viewType = viewType
        callback()
    }
}

@Immutable
data class ViewType(val title: String, val type: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeViewTypeAlertDialog(
    alertDialogState: AlertDialogState,
    selectedViewType: Int,
    modifier: Modifier = Modifier,
    onTypeChosen: (type: Int) -> Unit
) {
    val context = LocalContext.current
    val items = remember {
        listOf(
            ViewType(title = context.getString(R.string.grid), type = VIEW_TYPE_GRID),
            ViewType(title = context.getString(R.string.list), type = VIEW_TYPE_LIST)
        ).toImmutableList()
    }

    val groupTitles by remember {
        derivedStateOf { items.map { it.title } }
    }
    val (selected, setSelected) = remember { mutableStateOf(items.firstOrNull { it.type == selectedViewType }?.title) }
    BasicAlertDialog(onDismissRequest = alertDialogState::hide) {
        DialogSurface {
            Column(
                modifier = modifier
                    .padding(bottom = 18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                RadioGroupDialogComponent(
                    items = groupTitles,
                    selected = selected,
                    setSelected = { selectedTitle ->
                        setSelected(selectedTitle)
                    },
                    modifier = Modifier.padding(
                        vertical = SimpleTheme.dimens.padding.extraLarge,
                    ),
                    verticalPadding = SimpleTheme.dimens.padding.extraLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = SimpleTheme.dimens.padding.extraLarge)
                ) {
                    TextButton(onClick = {
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    TextButton(onClick = {
                        alertDialogState.hide()
                        onTypeChosen(getSelectedValue(items, selected))
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private fun getSelectedValue(
    items: ImmutableList<ViewType>,
    selected: String?
) = items.first { it.title == selected }.type

@MyDevices
@Composable
private fun ChangeViewTypeAlertDialogPreview() {
    AppThemeSurface {
        ChangeViewTypeAlertDialog(alertDialogState = rememberAlertDialogState(), selectedViewType = VIEW_TYPE_GRID) {}
    }
}
