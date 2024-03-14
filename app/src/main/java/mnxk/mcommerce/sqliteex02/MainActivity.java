package mnxk.mcommerce.sqliteex02;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import mnxk.mcommerce.adapters.CustomAdapter;
import mnxk.mcommerce.helpers.BookDao;
import mnxk.mcommerce.sqliteex02.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    ArrayList<String> pdlist;
    ArrayAdapter<String> pdadapter;
    CustomAdapter<String> BeerCustomAdapter;
    CustomAdapter<Book> BookCustomAdapter;
    ArrayList<Book> booklist;
    ArrayAdapter<Book> bookadapter;

    public static final String DATABASE_NAME = "product_db.db";
    public static final String DB_PATH = "/databases/";
    SQLiteDatabase Beerdb = null;


    public static final String TABLE_NAME = "Product";
    public static final String COLUMN_ID = "ProductId";
    public static final String COLUMN_NAME = "ProductName";
    public static final String COLUMN_PRICE = "ProductPrice";

    Button btnAdd, btnUpdate, btnDelete, btnNext, btnAddRecord, btnView;
    ListView listViewPd, listViewBook;
    TextView dialogTitle;

    TextInputEditText edtTitle, edtAuthor, edtPrice;
    TextInputLayout tilTitle, tilAuthor, tilPrice;

    boolean isBooklistShow = true;
    boolean isBeerlistShow = false;

    boolean isOnDeleteUpdateMode = false;

    boolean isViewBtnClicked = false;

    Dialog Adddialog, Updatedialog, Deletedialog;


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
        loadBeerDB();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.option_menu, menu);
//        return super .onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.add_menu) {
//            showAddDialog();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void convertToCustomAdapter() {
        BeerCustomAdapter = new CustomAdapter<>(this, pdlist);
        BookCustomAdapter = new CustomAdapter<>(this, booklist);
    }

    private void loadBeerDB() {
        Beerdb = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        if (Beerdb != null) {
            Log.e("DB", "Mở database thành công");
            pdlist.clear(); // Xóa dữ liệu cũ
            Cursor cursor = Beerdb.rawQuery("SELECT * FROM Product", null);
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
            Toast.makeText(this, "Beer Database Already Exists", Toast.LENGTH_SHORT).show();
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
        booklist = BookDao.getAllBooks(this);
        convertToCustomAdapter();
        pdadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pdlist);
        bookadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, booklist);
        // Ánh xạ
        btnView = binding.btnView;
        btnAdd = binding.btnAdd;
        btnView.setOnClickListener(v -> {
            isViewBtnClicked = true;
            onExitMode();
            if (isBooklistShow) {
                binding.listViewPd.setAdapter(BeerCustomAdapter);
                binding.btnView.setText("View Books");
                isBooklistShow = false;
                isBeerlistShow = true;
            } else {
                binding.listViewPd.setAdapter(BookCustomAdapter);
                isBooklistShow = true;
                isBeerlistShow = false;
                binding.btnView.setText("View Beers");
            }
        });
        btnAdd.setOnClickListener(v -> {
            showAddDialog();
        });
        binding.btnDelete.setOnClickListener(v -> {
            onExitMode();
            onDeleteMode();
        });
        binding.btnUpdate.setOnClickListener(v -> {
            onExitMode();
            onUpdateMode();
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

    private void onDeleteMode() {
        BeerCustomAdapter.setDeleteMode(true);
        BookCustomAdapter.setDeleteMode(true);
        isOnDeleteUpdateMode = true;
        if (itemCounts() > 0 && isViewBtnClicked) {
            binding.btnNext.setVisibility(VISIBLE);
            binding.btnNext.setText("Delete");
            binding.btnNext.setOnClickListener(v -> {
                deleteProduct();
                onExitMode();
                dropTableorDatabase();
            });
        } else if(itemCounts() == 0 && isViewBtnClicked) {
            Toast.makeText(this, "Please add record to the database", Toast.LENGTH_SHORT).show();
            binding.btnNext.setVisibility(GONE);
        } else {
            Toast.makeText(this, "Please view the list first", Toast.LENGTH_SHORT).show();
            binding.btnNext.setVisibility(GONE);
        }
    }

    private int itemCounts(){
        int count;
        if (isBeerlistShow) {
            count = BeerCustomAdapter.getCount();
        } else {
            count = BookCustomAdapter.getCount();
        }
        return count;
    }

    private void deleteProduct() {
        if (isBeerlistShow) {
            for (int i = 0; i < BeerCustomAdapter.getCount(); i++) {
                if (Boolean.TRUE.equals(BeerCustomAdapter.getItemCheckedStates().getOrDefault(i, false))) {
                    String[] id = BeerCustomAdapter.getItem(i).split(" - ");
                    Beerdb.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + id[0]);
                }
            }
            loadBeerDB();
            BeerCustomAdapter.clearCheckbox();
            BeerCustomAdapter.notifyDataSetChanged();

        } else {
            for (int i = 0; i < BookCustomAdapter.getCount(); i++) {
                if (Boolean.TRUE.equals(BookCustomAdapter.getItemCheckedStates().getOrDefault(i, false))) {
                    BookDao.deleteBook(this, BookCustomAdapter.getItem(i).getId());
                }
            }
            booklist.clear();
            booklist.addAll(BookDao.getAllBooks(this));
            BookCustomAdapter.clearCheckbox();
            BookCustomAdapter.notifyDataSetChanged();
        }
        Toast.makeText(this, "Delete successful", Toast.LENGTH_SHORT).show();
    }

    private void dropTableorDatabase() {
        if (isBeerlistShow || BeerCustomAdapter.getCount() == 0){
            // Delete beer database
            File dbFile = getDatabasePath(DATABASE_NAME);
            if (dbFile.exists()) {
                dbFile.delete();
                Toast.makeText(this, "Beer Database Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Beer Database Not Found", Toast.LENGTH_SHORT).show();
            }
        } else if (isBooklistShow || BookCustomAdapter.getCount() == 0){
            BookDao.dropTable(this);
            BookDao.createTable(this);
            booklist.clear();
            BookCustomAdapter.notifyDataSetChanged();
        } else {
        }
    }

    private void onUpdateMode() {
        BeerCustomAdapter.setUpdateMode(true);
        BookCustomAdapter.setUpdateMode(true);
        binding.btnNext.setText("Update");
        binding.btnNext.setVisibility(VISIBLE);
        isOnDeleteUpdateMode = true;
        binding.btnNext.setOnClickListener(v -> {
            showUpdateDialog();
        });
    }

    private void onExitMode() {
        BeerCustomAdapter.setDeleteMode(false);
        BeerCustomAdapter.setUpdateMode(false);
        BookCustomAdapter.setDeleteMode(false);
        BookCustomAdapter.setUpdateMode(false);
        binding.btnNext.setVisibility(GONE);
        isOnDeleteUpdateMode = false;
    }


    @Override
    public boolean onSupportNavigateUp() {
        if (isOnDeleteUpdateMode) {
            onExitMode();
            return true;
        } else if (Adddialog.isShowing()) {
            Adddialog.dismiss();
        } else if (Updatedialog.isShowing()) {
            Updatedialog.dismiss();
        } else if (Deletedialog.isShowing()) {
            Deletedialog.dismiss();
        } else {
            finish();
            return true;
        }
        return super.onSupportNavigateUp();
    }

    private void viewDetails() {
        // View product detials
    }

    private void showAddDialog() {
        Adddialog = new Dialog(this);
        Adddialog.setContentView(R.layout.custom_dialog);
        // clear nền cũ
        Adddialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Adddialog.setCancelable(true);
        Adddialog.show();
        btnAddRecord = Adddialog.findViewById(R.id.btn_add_record);
        edtTitle = Adddialog.findViewById(R.id.et_title);
        edtAuthor = Adddialog.findViewById(R.id.et_author);
        edtPrice = Adddialog.findViewById(R.id.et_price);
        tilTitle = Adddialog.findViewById(R.id.til_title);
        tilAuthor = Adddialog.findViewById(R.id.til_author);
        tilPrice = Adddialog.findViewById(R.id.til_price);
        dialogTitle = Adddialog.findViewById(R.id.dialog_name);
        onFocusHintConfig();
        if (isBeerlistShow) {
            dialogTitle.setText("Add New Beer");
            edtTitle.setHint("Beer Name");
            edtAuthor.setVisibility(GONE);
            tilAuthor.setVisibility(GONE);
            edtPrice.setHint("Beer Price");
        } else {
            dialogTitle.setText("Add New Book");
            edtTitle.setHint("Book Title");
            edtAuthor.setHint("Book Author");
            edtPrice.setHint("Book Price");
            edtAuthor.setVisibility(VISIBLE);
            tilAuthor.setVisibility(VISIBLE);
        }
        onExitMode();
        dialogControls();
    }

    private void showUpdateDialog() {
        Updatedialog = new Dialog(this);
        Updatedialog.setContentView(R.layout.update_dialog);
        // clear nền cũ
        Updatedialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Updatedialog.setCancelable(true);
        Updatedialog.show();
    }

    private void dialogControls() {
        btnAddRecord.setOnClickListener(v -> {
            if (isBeerlistShow) {
                if (checkValidAddFields()) {
                    insertRecordtoDB();
                    Adddialog.dismiss();
                }
            } else {
                if (checkValidAddFields()) {
                    insertRecordtoDB();
                    Adddialog.dismiss();
                }
            }
        });
    }

    private boolean checkValidAddFields() {
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
        if (isBeerlistShow) {
            String name = edtTitle.getText().toString(); // Lấy tên sản phẩm
            double price = Double.parseDouble(edtPrice.getText().toString()); // Lấy giá sản phẩm
            Beerdb.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '" + name + "', " + price + ")");
            loadBeerDB();
            BeerCustomAdapter.notifyDataSetChanged();
        } else {
            Book book = new Book(0, edtTitle.getText().toString(), edtAuthor.getText().toString(), Double.parseDouble(edtPrice.getText().toString()));
            BookDao.insertBook(this, book);
            booklist.clear();
            booklist.addAll(BookDao.getAllBooks(this));
            BookCustomAdapter.notifyDataSetChanged();
        }
    }
}