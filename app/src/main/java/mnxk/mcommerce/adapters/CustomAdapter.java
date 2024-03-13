package mnxk.mcommerce.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mnxk.mcommerce.sqliteex02.R;

public class CustomAdapter<T> extends ArrayAdapter<T> {
    private boolean isDeleteMode = false; // Flag to track delete mode
    private boolean isUpdateMode = false; // Flag to track update mode

    private int selectedPosition = -1; // Track the selected position for RadioButton

    private SparseBooleanArray itemCheckedStates; // Track the checked state of items

    public CustomAdapter(Context context, ArrayList<T> items) {
        super(context, 0, items);
        itemCheckedStates = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the custom item layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        // Get item data and views
        T item = getItem(position);
        TextView textView = convertView.findViewById(R.id.item_text);
        CheckBox checkBox = convertView.findViewById(R.id.checkbox);
        RadioButton radioButton = convertView.findViewById(R.id.radiobutton);


        radioButton.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId) {
                selectedPosition = position;
                notifyDataSetChanged(); // Refresh list view
            }
        });
        radioButton.setChecked(position == selectedPosition);

        // Bind item data to views
        assert item != null;
        textView.setText(item.toString());

        // Handle visibility of checkboxes and radio buttons based on mode
        if (isDeleteMode) {
            checkBox.setVisibility(View.VISIBLE);
            radioButton.setVisibility(View.GONE);
            checkBox.setChecked(itemCheckedStates.get(position));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                itemCheckedStates.put(position, isChecked);
            });
        } else if (isUpdateMode) {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void clearCheckbox() {
        for (int i = 0; i < itemCheckedStates.size(); i++) {
            itemCheckedStates.put(itemCheckedStates.keyAt(i), false);
        }
    }

    // Method to set delete mode
    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        notifyDataSetChanged(); // Refresh list view
    }

    // Method to set update mode
    public void setUpdateMode(boolean updateMode) {
        isUpdateMode = updateMode;
        notifyDataSetChanged(); // Refresh list view
    }

    public Map<Integer, Boolean> getItemCheckedStates() {
        Map<Integer, Boolean> checkedItems = new HashMap<>();
        for (int i = 0; i < itemCheckedStates.size(); i++) {
            checkedItems.put(itemCheckedStates.keyAt(i), itemCheckedStates.valueAt(i));
        }
        return checkedItems;
    }
}
