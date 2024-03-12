package mnxk.mcommerce.sqliteex02;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    Button btnAdd, btnUpdate, btnDelete, btnSearch, btnAddRecord, btnView;
    ListView listViewPd, listViewBook;

    TextInputEditText edtTitle, edtAuthor, edtPrice;
    TextInputLayout tilTitle, tilAuthor, tilPrice;

    boolean isBooklistShow = true;
    boolean isBeerlistShow = false;

    Dialog dialog;

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
        btnView = binding.btnView;
        btnAdd = binding.btnAdd;
        btnView.setOnClickListener(v -> {
            if (isBooklistShow) {
                binding.listViewPd.setAdapter(pdadapter);
                binding.btnView.setText("View Books");
                isBooklistShow = false;
                isBeerlistShow = true;
            } else {
                binding.listViewPd.setAdapter(bookadapter);
                isBooklistShow = true;
                isBeerlistShow = false;
                binding.btnView.setText("View Beers");
            }
        });
        btnAdd.setOnClickListener(v -> {
            showDialog();
        });
    }

    private void onFocusHintConfig() {
        edtTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isBeerlistShow) {
                tilTitle.setHint("Beer Name");
            } else if (hasFocus && isBooklistShow) {
                tilTitle.setHint("Book Title");
            } else {
                tilTitle.setHint("");
            }
        });
        edtAuthor.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isBooklistShow) {
                tilAuthor.setHint("Author");
            } else {
                tilAuthor.setHint("");
            }
        });
        edtPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isBeerlistShow) {
                tilPrice.setHint("Beer Price");
            } else if (hasFocus && isBooklistShow) {
                tilPrice.setHint("Book Price");
            } else {
                tilPrice.setHint("");
            }
        });
    }

    private void showDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        // clear nền cũ
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);
        dialog.show();
        btnAddRecord = dialog.findViewById(R.id.btn_add_record);
        edtTitle = dialog.findViewById(R.id.et_title);
        edtAuthor = dialog.findViewById(R.id.et_author);
        edtPrice = dialog.findViewById(R.id.et_price);
        tilTitle = dialog.findViewById(R.id.til_title);
        tilAuthor = dialog.findViewById(R.id.til_author);
        tilPrice = dialog.findViewById(R.id.til_price);
        onFocusHintConfig();
        if (isBeerlistShow) {
            edtTitle.setHint("Beer Name");
            edtAuthor.setVisibility(GONE);
            tilAuthor.setVisibility(GONE);
            edtPrice.setHint("Beer Price");
        } else {
            edtTitle.setHint("Book Title");
            edtAuthor.setHint("Book Author");
            edtPrice.setHint("Book Price");
            edtAuthor.setVisibility(VISIBLE);
            tilAuthor.setVisibility(VISIBLE);
        }
        dialogControls();
    }

    private void dialogControls() {
//        btnAddRecord.setOnClickListener(v -> {
//            if (checkValidFields()) {
//                insertRecordtoDB();
//                dialog.dismiss();
//            }
//        });
        btnAddRecord.setOnClickListener(v -> {
            if (isBeerlistShow) {
                if (checkValidFields()) {
                    insertRecordtoDB();
                    dialog.dismiss();
                }
            } else {
                if (checkValidFields()) {
                    insertRecordtoDB();
                    dialog.dismiss();
                }
            }
        });
    }

    private boolean checkValidFields() {
        if (isBeerlistShow) {
            if (edtTitle.getText().toString().isEmpty() || edtPrice.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            } else if (edtPrice.getText().toString().matches(".*[a-zA-Z]+.*")) {
                Toast.makeText(this, "Price must be a number", Toast.LENGTH_SHORT).show();
                return false;
            } else if (Double.parseDouble(edtPrice.getText().toString()) <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else {
            if (edtTitle.getText().toString().isEmpty() || edtAuthor.getText().toString().isEmpty() || edtPrice.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            } else if (edtPrice.getText().toString().matches(".*[a-zA-Z]+.*")) {
                Toast.makeText(this, "Price must be a number", Toast.LENGTH_SHORT).show();
                return false;
            } else if (Double.parseDouble(edtPrice.getText().toString()) <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        }
    }

    private void insertRecordtoDB() {
//        Book book = new Book(0, edtTitle.getText().toString(), edtAuthor.getText().toString(), Double.parseDouble(edtPrice.getText().toString()));
//        BookDao.insertBook(this, book);
//        booklist.clear();
//        booklist.addAll(BookDao.getAllBooks(this));
//        bookadapter.notifyDataSetChanged();
//    }
        if (isBeerlistShow) {
            String name = edtTitle.getText().toString();
            double price = Double.parseDouble(edtPrice.getText().toString());
            db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '" + name + "', " + price + ")");
            loadDB();
        } else {
            Book book = new Book(0, edtTitle.getText().toString(), edtAuthor.getText().toString(), Double.parseDouble(edtPrice.getText().toString()));
            BookDao.insertBook(this, book);
            booklist.clear();
            booklist.addAll(BookDao.getAllBooks(this));
            bookadapter.notifyDataSetChanged();
        }
    }
}