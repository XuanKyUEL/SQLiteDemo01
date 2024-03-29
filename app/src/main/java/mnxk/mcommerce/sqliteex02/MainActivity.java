package mnxk.mcommerce.sqliteex02;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

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

    AppCompatEditText edtTitle, edtAuthor, edtPrice;

    AppCompatEditText etUpdateNameBeer, etUpdatePriceBeer, etUpdateTitle, etUpdateAuthor, etUpdatePriceBook;
    TextInputLayout tilTitle, tilAuthor, tilPrice;

    private String initialNameBeer, initialPriceBeer, initialTitle, initialAuthor, initialPriceBook;

    boolean isBooklistShow = true;
    boolean isBeerlistShow = false;

    boolean isOnDeleteUpdateMode = false;

    boolean isViewBtnClicked = false;

    int selectedPositionRadio = -1;

    int beerDetailsId = -1;
    int bookDetailsId = -1;


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
            binding.tvViewAll.setVisibility(VISIBLE);
            onExitMode();
            if (isBooklistShow) {
                binding.listViewPd.setAdapter(BeerCustomAdapter);
                binding.btnView.setText("View Books");
                binding.tvViewAll.setText("Viewing Beers");
                isBooklistShow = false;
                isBeerlistShow = true;
            } else {
                binding.listViewPd.setAdapter(BookCustomAdapter);
                isBooklistShow = true;
                isBeerlistShow = false;
                binding.btnView.setText("View Beers");
                binding.tvViewAll.setText("Viewing Books");
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


    private void onDeleteMode() {
        BeerCustomAdapter.setDeleteMode(true);
        BookCustomAdapter.setDeleteMode(true);
        isOnDeleteUpdateMode = true;
        if (itemCounts() > 0 && isViewBtnClicked) {
            binding.btnNext.setVisibility(VISIBLE);
            binding.btnNext.setText("Delete");
            binding.btnNext.setOnClickListener(v -> {
                confirmDelete();
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
        boolean itemSelected = false;
        if (isBeerlistShow) {
            for (int i = 0; i < BeerCustomAdapter.getCount(); i++) {
                if (Boolean.TRUE.equals(BeerCustomAdapter.getItemCheckedStates().getOrDefault(i, false))) {
                    String[] id = BeerCustomAdapter.getItem(i).split(" - ");
                    Beerdb.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + id[0]);
                    itemSelected = true;
                }
            }
            if (itemSelected) {
                loadBeerDB();
                BeerCustomAdapter.clearCheckbox();
                BeerCustomAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Delete successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            for (int i = 0; i < BookCustomAdapter.getCount(); i++) {
                if (Boolean.TRUE.equals(BookCustomAdapter.getItemCheckedStates().getOrDefault(i, false))) {
                    BookDao.deleteBook(this, BookCustomAdapter.getItem(i).getId());
                    itemSelected = true;
                }
            }
            if (itemSelected) {
                booklist.clear();
                booklist.addAll(BookDao.getAllBooks(this));
                BookCustomAdapter.clearCheckbox();
                BookCustomAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Delete successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to delete the selected items?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteProduct();
            onExitMode();
            if (itemCounts() == 0) {
                dropTableorDatabase();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.confirm_dialog_bg);
    }

    private void dropTableorDatabase() {
        if (isBeerlistShow){
            // Delete beer database
            File dbFile = getDatabasePath(DATABASE_NAME);
            if (dbFile.exists()) {
                dbFile.delete();
                Toast.makeText(this, "Beer Database Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Beer Database Not Found", Toast.LENGTH_SHORT).show();
            }
        } else if (isBooklistShow){
            BookDao.dropTable(this);
            BookDao.createTable(this);
            booklist.clear();
            BookCustomAdapter.notifyDataSetChanged();
        }
    }

    private void onUpdateMode() {
        if (itemCounts() > 0) {
            BeerCustomAdapter.setUpdateMode(true);
            BookCustomAdapter.setUpdateMode(true);
            binding.btnNext.setText("Update");
            binding.btnNext.setVisibility(VISIBLE);
            isOnDeleteUpdateMode = true;
            binding.btnNext.setOnClickListener(v -> {
                showUpdateDialog();
            });
        } else {
            Toast.makeText(this, "Please add an item before updating", Toast.LENGTH_SHORT).show();
        }
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

    private void showAddDialog() {
        Adddialog = new Dialog(this);
        Adddialog.setContentView(R.layout.add_dialog);
        // clear nền cũ
        Adddialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Adddialog.setCancelable(true);
        Adddialog.show();
        btnAddRecord = Adddialog.findViewById(R.id.btn_add_record);
        edtAuthor = Adddialog.findViewById(R.id.et_author);
        edtTitle = Adddialog.findViewById(R.id.et_title);
        edtPrice = Adddialog.findViewById(R.id.et_price);
        tilAuthor = Adddialog.findViewById(R.id.til_author);
        dialogTitle = Adddialog.findViewById(R.id.dialog_name);
        if (isBeerlistShow) {
            dialogTitle.setText("Add New Beer");
            edtAuthor.setVisibility(GONE);
            tilAuthor.setVisibility(GONE);
        } else {
            dialogTitle.setText("Add New Book");
            edtAuthor.setVisibility(VISIBLE);
            tilAuthor.setVisibility(VISIBLE);
        }
        onExitMode();
        dialogAddControls();
    }

    private void showUpdateDialog() {
        Updatedialog = new Dialog(this);
        // clear nền cũ
        Updatedialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Updatedialog.setCancelable(true);
        if (isBeerlistShow) {
            Updatedialog.setContentView(R.layout.beer_update_dialog);
            btnUpdate = Updatedialog.findViewById(R.id.btn_update_record_beer);
            // ánh xạ các edittext
            etUpdateNameBeer = Updatedialog.findViewById(R.id.et_update_name_beer);
            etUpdatePriceBeer = Updatedialog.findViewById(R.id.et_update_price_beer);
            // store initial values
            // get selected item via radio button
            if (BeerCustomAdapter.getCount() > 0) {
                selectedPositionRadio = BeerCustomAdapter.getSelectedPositionRadio();
                int i = selectedPositionRadio;
                Log.i("SelectedPosition", String.valueOf(selectedPositionRadio));
                if (i != -1) {
                    String beerDetails = BeerCustomAdapter.getItem(i);
                    beerDetailsId = Integer.parseInt(beerDetails.split(" - ")[0]);
                    etUpdateNameBeer.setText(BeerCustomAdapter.getItem(i).split(" - ")[1]);
                    etUpdatePriceBeer.setText(BeerCustomAdapter.getItem(i).split(" - ")[2]);
                    initialNameBeer = etUpdateNameBeer.getText().toString();
                    initialPriceBeer = etUpdatePriceBeer.getText().toString();
                    Updatedialog.show();
                    dialogUpdateControls();
                    onExitMode();
                } else
                    Toast.makeText(this, "Please select an item to update", Toast.LENGTH_SHORT).show();
            }
        } else {
            Updatedialog.setContentView(R.layout.book_update_dialog);
            btnUpdate = Updatedialog.findViewById(R.id.btn_update_record_book);
            // ánh xạ các edittext
            etUpdateTitle = Updatedialog.findViewById(R.id.et_update_title);
            etUpdateAuthor = Updatedialog.findViewById(R.id.et_update_author);
            etUpdatePriceBook = Updatedialog.findViewById(R.id.et_update_price_book);
            // store initial values
            // get selected item via radio button
            if (BookCustomAdapter.getCount() > 0) {
                selectedPositionRadio = BookCustomAdapter.getSelectedPositionRadio();
                int i_book = selectedPositionRadio;
                if (i_book != -1) {
                    String bookDetails = String.valueOf(BookCustomAdapter.getItem(i_book));
                    bookDetailsId = Integer.parseInt(bookDetails.split(" - ")[0]);
                    etUpdateTitle.setText(BookCustomAdapter.getItem(i_book).getTitle());
                    etUpdateAuthor.setText(BookCustomAdapter.getItem(i_book).getAuthor());
                    etUpdatePriceBook.setText(String.valueOf(BookCustomAdapter.getItem(i_book).getPrice()));
                    initialTitle = etUpdateTitle.getText().toString();
                    initialAuthor = etUpdateAuthor.getText().toString();
                    initialPriceBook = etUpdatePriceBook.getText().toString();
                    Updatedialog.show();
                    dialogUpdateControls();
                    onExitMode();
                } else {
                    Toast.makeText(this, "Please select an item to update", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void dialogUpdateControls() {
        btnUpdate.setOnClickListener(v -> {
            if (isBeerlistShow) {
                if (checkValidUpdateFields()) {
                    updateRecordtoDB();
                    Updatedialog.dismiss();
                }
            } else {
                if (checkValidUpdateFields()) {
                    updateRecordtoDB();
                    Updatedialog.dismiss();
                }
            }
        });
    }

    private void updateRecordtoDB() {
        if (isBeerlistShow) {
            String name = etUpdateNameBeer.getText().toString(); // Lấy tên sản phẩm
            double price = Double.parseDouble(etUpdatePriceBeer.getText().toString()); // Lấy giá sản phẩm
            Beerdb.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME + " = '" + name + "', " + COLUMN_PRICE + " = " + price + " WHERE " + COLUMN_ID + " = " + beerDetailsId);
            Log.i("UpdateBeer", "Updated record: " + beerDetailsId + " - " + name + " - " + price);
            loadBeerDB();
            Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show();
            BeerCustomAdapter.notifyDataSetChanged();
        } else {
            Book book = new Book(bookDetailsId, etUpdateTitle.getText().toString(), etUpdateAuthor.getText().toString(), Double.parseDouble(etUpdatePriceBook.getText().toString()));
            Log.i("UpdateBook", "Updated record: " + bookDetailsId + " - " + etUpdateTitle.getText().toString() + " - " + etUpdateAuthor.getText().toString() + " - " + Double.parseDouble(etUpdatePriceBook.getText().toString()));
            BookDao.updateBook(this, book);
            booklist.clear();
            booklist.addAll(BookDao.getAllBooks(this));
            Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show();
            BookCustomAdapter.notifyDataSetChanged();
        }
    }

    private void dialogAddControls() {
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

    private boolean checkValidUpdateFields() {
        if (isBeerlistShow) {
            if (etUpdateNameBeer.getText().toString().isEmpty() || etUpdatePriceBeer.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            } else if (etUpdatePriceBeer.getText().toString().matches(".*[a-zA-Z]+.*")) {
                Toast.makeText(this, "Price must be a number", Toast.LENGTH_SHORT).show();
                return false;
            } else if (Double.parseDouble(etUpdatePriceBeer.getText().toString()) <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            } else if (etUpdateNameBeer.getText().toString().equals(initialNameBeer) && etUpdatePriceBeer.getText().toString().equals(initialPriceBeer)){
                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else {
            if (etUpdateTitle.getText().toString().isEmpty() || etUpdateAuthor.getText().toString().isEmpty() || etUpdatePriceBook.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            } else if (etUpdatePriceBook.getText().toString().matches(".*[a-zA-Z]+.*")) {
                Toast.makeText(this, "Price must be a number", Toast.LENGTH_SHORT).show();
                return false;
            } else if (Double.parseDouble(etUpdatePriceBook.getText().toString()) <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return false; 
            } else if (etUpdateTitle.getText().toString().equals(initialTitle) && etUpdateAuthor.getText().toString().equals(initialAuthor) && etUpdatePriceBook.getText().toString().equals(initialPriceBook)){
                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Add successful", Toast.LENGTH_SHORT).show();
        } else {
            Book book = new Book(0, edtTitle.getText().toString(), edtAuthor.getText().toString(), Double.parseDouble(edtPrice.getText().toString()));
            BookDao.insertBook(this, book);
            booklist.clear();
            booklist.addAll(BookDao.getAllBooks(this));
            BookCustomAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Add successful", Toast.LENGTH_SHORT).show();
        }
    }
}