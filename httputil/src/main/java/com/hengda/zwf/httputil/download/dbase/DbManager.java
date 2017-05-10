package com.hengda.zwf.httputil.download.dbase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.hengda.zwf.httputil.download.entity.DownloadFlag;
import com.hengda.zwf.httputil.download.entity.DownloadMission;
import com.hengda.zwf.httputil.download.entity.DownloadRecord;
import com.hengda.zwf.httputil.download.entity.DownloadStatus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DbManager {

    private volatile static DbManager singleton;
    private volatile SQLiteDatabase readableDatabase;
    private volatile SQLiteDatabase writableDatabase;
    private final Object databaseLock = new Object();
    private DbOpenHelper mDbOpenHelper;

    private DbManager(Context context) {
        mDbOpenHelper = new DbOpenHelper(context);
    }

    public static DbManager getSingleton(Context context) {
        if (singleton == null) {
            synchronized (DbManager.class) {
                if (singleton == null) {
                    singleton = new DbManager(context);
                }
            }
        }
        return singleton;
    }

    private SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = writableDatabase;
        if (db == null) {
            synchronized (databaseLock) {
                db = writableDatabase;
                if (db == null) {
                    db = writableDatabase = mDbOpenHelper.getWritableDatabase();
                }
            }
        }
        return db;
    }

    private SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = readableDatabase;
        if (db == null) {
            synchronized (databaseLock) {
                db = readableDatabase;
                if (db == null) {
                    db = readableDatabase = mDbOpenHelper.getReadableDatabase();
                }
            }
        }
        return db;
    }

    public boolean recordExists(String url) {
        return !recordNotExists(url);
    }

    public boolean recordNotExists(String url) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(RecordTable.TABLE_NAME, new String[]{RecordTable.COLUMN_ID},
                    "url=?", new String[]{url}, null, null, null);
            return cursor.getCount() == 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public long insertRecord(DownloadMission mission) {
        return getWritableDatabase().insert(RecordTable.TABLE_NAME, null, RecordTable.insert(mission));
    }

    public long updateRecord(String url, DownloadStatus status) {
        return getWritableDatabase().update(RecordTable.TABLE_NAME, RecordTable.update(status), "url=?", new String[]{url});
    }

    public long updateRecord(String url, int flag) {
        return getWritableDatabase().update(RecordTable.TABLE_NAME, RecordTable.update(flag), "url=?", new String[]{url});
    }

    public int deleteRecord(String url) {
        return getWritableDatabase().delete(RecordTable.TABLE_NAME, "url=?", new String[]{url});
    }

    public DownloadRecord readSingleRecord(String url) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select * from " + RecordTable.TABLE_NAME + " where url=?", new String[]{url});
            cursor.moveToFirst();
            return RecordTable.read(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public DownloadStatus readStatus(String url) {
        Cursor cursor = null;
        try {
            String[] columns = {RecordTable.COLUMN_DOWNLOAD_SIZE, RecordTable.COLUMN_TOTAL_SIZE, RecordTable.COLUMN_IS_CHUNKED};
            cursor = getReadableDatabase().query(RecordTable.TABLE_NAME, columns, "url=?", new String[]{url}, null, null, null);
            if (cursor.getCount() == 0) {
                return new DownloadStatus();
            } else {
                cursor.moveToFirst();
                return RecordTable.readStatus(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void closeDataBase() {
        synchronized (databaseLock) {
            readableDatabase = null;
            writableDatabase = null;
            mDbOpenHelper.close();
        }
    }

    public Observable<DownloadRecord> readRecord(final String url) {
        return Observable
                .create(new ObservableOnSubscribe<DownloadRecord>() {
                    @Override
                    public void subscribe(ObservableEmitter<DownloadRecord> emitter) throws Exception {
                        Cursor cursor = null;
                        try {
                            String sql = "select * from " + RecordTable.TABLE_NAME + " where url=?";
                            cursor = getReadableDatabase().rawQuery(sql, new String[]{url});
                            if (cursor.getCount() == 0) {
                                emitter.onNext(new DownloadRecord());
                            } else {
                                cursor.moveToFirst();
                                emitter.onNext(RecordTable.read(cursor));
                            }
                            emitter.onComplete();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<DownloadRecord>> readAllRecords() {
        return Observable
                .create(new ObservableOnSubscribe<List<DownloadRecord>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<DownloadRecord>> emitter)
                            throws Exception {
                        Cursor cursor = null;
                        try {
                            cursor = getReadableDatabase().rawQuery("select * from " + RecordTable.TABLE_NAME, new String[]{});
                            List<DownloadRecord> result = new ArrayList<>();
                            while (cursor.moveToNext()) {
                                result.add(RecordTable.read(cursor));
                            }
                            emitter.onNext(result);
                            emitter.onComplete();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public long repairErrorFlag() {
        return getWritableDatabase().update(RecordTable.TABLE_NAME, RecordTable.update(DownloadFlag.PAUSED),
                RecordTable.COLUMN_DOWNLOAD_FLAG + "=? or " + RecordTable.COLUMN_DOWNLOAD_FLAG + "=?",
                new String[]{DownloadFlag.WAITING + "", DownloadFlag.STARTED + ""});
    }

}