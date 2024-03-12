package mnxk.mcommerce.sqliteex02;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import mnxk.mcommerce.adapters.Book;
import mnxk.mcommerce.helpers.BookDao;
import mnxk.mcommerce.sqliteex02.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    ArrayList<String> pdlist;
    ArrayAdapter<String> pdadapter;
    ArrayList<Book> booklist;
    ArrayAdapter<Book> bookadapter;

    public static final String DATABASE_NAME = "product_db.db";
    public static final String DB_PATH = "/databases/";
    SQLiteDatabase db = null;

    public static final String TABLE_NAME = "Product";
    public static final String COLUMN_ID = "ProductId";
    public static final String COLUMN_NAME = "ProductName";
    public static final String COLUMN_PRICE = "ProductPrice";

    Button btnAdd, btnUpdate, btnDelete, btnSearch;
    ListView listViewPd, listViewBook;

    boolean isBooklistShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        copyDBfromAssets();
        addControls();
        loadDB();
    }

    private void loadDB() {
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        if (db != null) {
            Log.e("DB", "Mở database thành công");
            pdlist.clear(); // Xóa dữ liệu cũ
            Cursor cursor = db.rawQuery("SELECT * FROM Product", null);
            if (cursor != null) { // Kiểm tra xem có dữ liệu không
                while (cursor.moveToNext()) { // Di chuyển con trỏ đến từng dòng dữ liệu
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    double price = cursor.getDouble(2);
                    pdlist.add(id + " - " + name + " - " + price); // Thêm dữ liệu vào ArrayList
                    Log.e("Product", id + " - " + name + " - " + price); // In ra Logcat
                }
                cursor.close(); // Đóng con trỏ
                pdadapter.notifyDataSetChanged(); // Cập nhật lại ListView
            }
        } else {
            Log.e("DB", "Mở database thất bại");
        }
    }

    private void copyDBfromAssets() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            copyDB();
            Toast.makeText(this, "Database Copied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Database Already Exists", Toast.LENGTH_SHORT).show();
            dbFile.delete();
            copyDB();
            Toast.makeText(this, "Database Re-copied successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyDB() {
        // Khúc này cần try và catch
        try {
            // Tạo một thư mục để lưu trữ database
            File dbFolder = new File(getApplicationInfo().dataDir + DB_PATH);
            if (!dbFolder.exists()) {
                dbFolder.mkdir();
            }
            // Tạo một file để lưu trữ database
            File dbFile = new File(getApplicationInfo().dataDir + DB_PATH + DATABASE_NAME);
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            // Sao chép database từ thư mục assets vào thư mục lưu trữ
            InputStream is = getAssets().open(DATABASE_NAME);
            OutputStream os = new FileOutputStream(dbFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Lỗi sao chép", e.toString());
        }
    }

    private void addControls() {
        pdlist = new ArrayList<>();
        pdadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pdlist);
        booklist = BookDao.getAllBooks(this);
        bookadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, booklist);
        // Ánh xạ
        binding.btnView.setOnClickListener(v -> {
            if (isBooklistShow) {
                binding.listViewPd.setAdapter(pdadapter);
                binding.btnView.setText("View Books");
                isBooklistShow = false;
            } else {
                binding.listViewPd.setAdapter(bookadapter);
                isBooklistShow = true;
                binding.btnView.setText("View Beers");
            }
        });
    }
}