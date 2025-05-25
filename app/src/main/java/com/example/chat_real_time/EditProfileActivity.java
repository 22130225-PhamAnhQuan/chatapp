package com.example.chat_real_time;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends Activity {

    private EditText editFName, editLName, editemail, editPhone, editAddress;
    private Button saveButton, cancelButton;

    private DatabaseReference userRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info); // layout popup của bạn

        editFName = findViewById(R.id.editFName);
        editLName = findViewById(R.id.editLName);
        editemail = findViewById(R.id.editemail);   // Phone theo layout bạn
        editPhone = findViewById(R.id.editPhone);   // Bio theo layout bạn
        editAddress = findViewById(R.id.editAddress);

        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Load dữ liệu user hiện tại
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                editFName.setText(task.getResult().child("firstName").getValue(String.class));
                editLName.setText(task.getResult().child("lastName").getValue(String.class));
                editemail.setText(task.getResult().child("phoneNumber").getValue(String.class));
                editPhone.setText(task.getResult().child("bio").getValue(String.class));
                editAddress.setText(task.getResult().child("address").getValue(String.class));
            } else {
                Toast.makeText(EditProfileActivity.this, "Không tải được thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> {
            String firstName = editFName.getText().toString().trim();
            String lastName = editLName.getText().toString().trim();
            String phone = editemail.getText().toString().trim();
            String bio = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();

            if (TextUtils.isEmpty(firstName)) {
                editFName.setError("Không được để trống tên");
                editFName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(lastName)) {
                editLName.setError("Không được để trống họ");
                editLName.requestFocus();
                return;
            }

            // Bạn có thể thêm kiểm tra phone, bio, address tùy ý

            userRef.child("firstName").setValue(firstName);
            userRef.child("lastName").setValue(lastName);
            userRef.child("phoneNumber").setValue(phone);
            userRef.child("bio").setValue(bio);
            userRef.child("address").setValue(address)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);  // <-- Thêm dòng này để báo kết quả thành công
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        cancelButton.setOnClickListener(v -> finish());
    }
}
