package mnxk.mcommerce.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import mnxk.mcommerce.adapters.Book;

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
}
