package com.msahil432.sms.feature.backup

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.msahil432.sms.R
import com.moez.QKSMS.common.base.FlowableAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.DateFormatter
import com.msahil432.sms.model.BackupFile
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.backup_list_item.view.*
import javax.inject.Inject

class BackupAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) : FlowableAdapter<BackupFile>() {

    val backupSelected: Subject<BackupFile> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.backup_list_item, parent, false)

        return QkViewHolder(view).apply {
            view.setOnClickListener { backupSelected.onNext(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val backup = getItem(position)
        val view = holder.containerView

        val count = backup.messages

        view.title.text = dateFormatter.getDetailedTimestamp(backup.date)
        view.messages.text = context.resources.getQuantityString(R.plurals.backup_message_count, count, count)
        view.size.text = Formatter.formatFileSize(context, backup.size)
    }

}