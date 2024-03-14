package mnxk.mcommerce.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import mnxk.mcommerce.adapters.Book;
import mnxk.mcommerce.sqliteex02.MainActivity;

public class BookDao {
    public static ArrayList<Book> getAllBooks(Context context) {
        ArrayList<Book> books = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(context, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Book", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String author = cursor.getString(2);
            double price = cursor.getDouble(3);
            Book book = new Book(id, title, author, price);
            books.add(book);
        }
        cursor.close();
        db.close();
        return books;
    }

    // Insert a new book
    public static void insertBook(Context context, Book book) {
        DbHelper dbHelper = new DbHelper(context, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Insert into Book (title, author, price) values ('" + book.getTitle() + "', '" + book.getAuthor() + "', " + book.getPrice() + ")";
        db.execSQL(sql);
        db.close();
    }

    public static void updateBook(Context context, Book book) {
        DbHelper dbHelper = new DbHelper(context, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Update Book set title = '" + book.getTitle() + "', author = '" + book.getAuthor() + "', price = " + book.getPrice() + " where id = " + book.getId();
        db.execSQL(sql);
        db.close();
    }

    public static void deleteBook(Context context, int id) {
        DbHelper dbHelper = new DbHelper(context, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Delete from Book where id = " + id;
        db.execSQL(sql);
        db.close();
    }

    public static void createTable(Context context) {
        DbHelper dbHelper = new DbHelper(context, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Create table Book (id integer primary key autoincrement, title text, author text, price real)";
        db.execSQL(sql);
        db.close();
    }


    public static void dropTable(MainActivity mainActivity) {
        DbHelper dbHelper = new DbHelper(mainActivity, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Drop table if exists Book";
        db.execSQL(sql);

        db.close();
    }

    public static boolean isTableExists(MainActivity mainActivity) {
        DbHelper dbHelper = new DbHelper(mainActivity, "Book", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Book'", null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }
}
