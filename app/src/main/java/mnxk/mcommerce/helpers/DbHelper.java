package mnxk.mcommerce.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "Create table Book (id integer primary key autoincrement, title text, author text, price real)"; // Tạo bảng Book
        db.execSQL(sql);
        sql = "Insert into Book (title, author, price) values ('Harry Porter 1', 'J.K.Rowling', 20000)"; // Thêm dữ liệu mẫu
        db.execSQL(sql);
        sql = "Insert into Book (title, author, price) values ('Percy Jackson', 'Rick Riordan', 30000)"; // Thêm dữ liệu mẫu
        db.execSQL(sql);
        sql = "Insert into Book (title, author, price) values ('Những con chim Ngạn', 'Nguyễn Nguyễn', '15000')"; // Thêm dữ liệu mẫu
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "Drop table if exists Book"; // Xóa bảng Book nếu tồn tại
        db.execSQL(sql);
        onCreate(db);
    }
    public int getIdByPosition(SQLiteDatabase db, int position) throws Exception {
        String sql = "Select id from Book limit " + position + ", 1";
        int id = -1;
        try {
            id = (int) db.compileStatement(sql).simpleQueryForLong();
        } catch (Exception e) {
            throw new Exception("Error executing SQL query", e);
        }
        return id;
    }
}
