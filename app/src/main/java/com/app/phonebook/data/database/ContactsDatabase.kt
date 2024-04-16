package com.app.phonebook.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.phonebook.base.helpers.Converters
import com.app.phonebook.base.utils.FIRST_CONTACT_ID
import com.app.phonebook.base.utils.FIRST_GROUP_ID
import com.app.phonebook.base.utils.getEmptyLocalContact
import com.app.phonebook.data.dao.ContactsDao
import com.app.phonebook.data.dao.GroupsDao
import com.app.phonebook.data.models.Group
import com.app.phonebook.data.models.LocalContact
import java.util.concurrent.Executors

@Database(entities = [LocalContact::class, Group::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun ContactsDao(): ContactsDao

    abstract fun GroupsDao(): GroupsDao

    companion object {
        private var db: ContactsDatabase? = null

        fun getInstance(context: Context): ContactsDatabase {
            if (db == null) {
                synchronized(ContactsDatabase::class) {
                    db = Room.databaseBuilder(
                        context.applicationContext, ContactsDatabase::class.java, "local_contacts.db"
                    ).addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            increaseAutoIncrementIds()
                        }
                    }).allowMainThreadQueries().build()
                }
            }

            return db!!
        }

//        fun destroyInstance() {
//            db = null
//        }

        // start autoincrement ID from FIRST_CONTACT_ID/FIRST_GROUP_ID to avoid conflicts
        // Room doesn't seem to have a built in way for it, so just create a contact/group and delete it
        private fun increaseAutoIncrementIds() {
            Executors.newSingleThreadExecutor().execute {
                val emptyContact = getEmptyLocalContact()
                emptyContact.id = FIRST_CONTACT_ID
                db?.ContactsDao()?.apply {
                    insertOrUpdate(emptyContact)
                    deleteContactId(FIRST_CONTACT_ID)
                }

                val emptyGroup = Group(FIRST_GROUP_ID, "")
                db?.GroupsDao()?.apply {
                    insertOrUpdate(emptyGroup)
                    deleteGroupId(FIRST_GROUP_ID)
                }
            }
        }
    }
}
