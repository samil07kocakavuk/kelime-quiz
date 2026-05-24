package com.samil.kelimequiz.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;

import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.testing.MigrationTestHelper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseMigrationTest {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase.class.getCanonicalName(),
            new androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
    );

    @Test
    public void migrateFromVersion1To8_preservesUserAndAddsCefrColumn() throws Exception {
        SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 1);
        database.execSQL("INSERT INTO users(userId, username, passwordHash, passwordSalt, passwordIterations, createdAt) VALUES(1, 'demo', 'hash', 'salt', 1, 1)");
        database.close();

        AppDatabase migrated = Room.databaseBuilder(
                        InstrumentationRegistry.getInstrumentation().getTargetContext(),
                        AppDatabase.class,
                        TEST_DB
                )
                .addMigrations(
                        AppDatabase.MIGRATION_1_2,
                        AppDatabase.MIGRATION_2_4,
                        AppDatabase.MIGRATION_3_4,
                        AppDatabase.MIGRATION_4_5,
                        AppDatabase.MIGRATION_5_6,
                        AppDatabase.MIGRATION_6_7,
                        AppDatabase.MIGRATION_7_8
                )
                .build();

        SupportSQLiteDatabase migratedDb = migrated.getOpenHelper().getWritableDatabase();

        Cursor userCursor = migratedDb.query("SELECT username FROM users WHERE userId = 1");
        try {
            assertTrue(userCursor.moveToFirst());
            assertEquals("demo", userCursor.getString(0));
        } finally {
            userCursor.close();
        }

        Cursor wordColumns = migratedDb.query("PRAGMA table_info(words)");
        boolean hasCefrLevel = false;
        try {
            while (wordColumns.moveToNext()) {
                if ("cefrLevel".equals(wordColumns.getString(wordColumns.getColumnIndexOrThrow("name")))) {
                    hasCefrLevel = true;
                    break;
                }
            }
        } finally {
            wordColumns.close();
        }

        assertTrue(hasCefrLevel);
        migrated.close();
    }
}
