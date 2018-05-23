package cn.kkmofang.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONException;
import cn.kkmofang.script.JSON;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by hailong11 on 2018/5/23.
 */

public class SqliteStorage implements IStorage {

    private final SQLiteDatabase _db;
    private final String _name;

    public SqliteStorage(SQLiteDatabase db, String name) {
        _db = db;
        _name = name;
        try {
            _db.execSQL("CREATE TABLE IF NOT EXISTS [_" + name + "](key VARCHAR(4096) PRIMARY KEY , value TEXT)");
        }
        catch (Throwable ex) {
            Log.d("kk",Log.getStackTraceString(ex));
        }
    }

    protected Object decodeValue(String text) {
        if(text != null ) {
            if(text.startsWith("{") || text.startsWith("[")) {
                try {
                    return JSON.decodeString(text);
                } catch (JSONException e) {
                    Log.d("kk",Log.getStackTraceString(e));
                }
            } else {
                return text;
            }
        }
        return null;
    }

    protected String encodeValue(Object object) {
        try {
            return JSON.encodeObject(object);
        } catch (JSONException e) {
        }
        return ScriptContext.stringValue(object,"");
    }

    @Override
    public Object get(String key) {

        Object v = null;

        try {

            Cursor rs = _db.query("_" + _name, new String[]{"value"},"key=@key",new String[]{key},null,null,null,null);

            try {

                if(rs.moveToNext()) {
                    String text = rs.getString(0);
                    v = decodeValue(text);
                }

            } finally {
                rs.close();
            }

        } catch (Throwable ex) {
            Log.d("kk",Log.getStackTraceString(ex));
        }

        return v;
    }

    @Override
    public void set(String key, Object value) {

        try {

            boolean has = false;

            Cursor rs = _db.query("_" + _name, new String[]{"value"},"key=@key",new String[]{key},null,null,null,null);

            try {

                if(rs.moveToNext()) {
                    has = true;
                }

            } finally {
                rs.close();
            }

            if(has) {
                _db.execSQL("UPDATE [_"+_name+"] SET [value]=@value WHERE [key]=@key",new Object[]{encodeValue(value),key});
            } else {
                _db.execSQL("INSERT INTO [_" +_name+ "]([key],[value]) VALUES (@key,@value) ",new Object[]{key,encodeValue(value)});
            }

        } catch (Throwable ex) {
            Log.d("kk",Log.getStackTraceString(ex));
        }

    }
}
