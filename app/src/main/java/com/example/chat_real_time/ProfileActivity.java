package com.example.chat_real_time;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageProfileAvatar;
    private static final int EDIT_PROFILE_REQUEST_CODE = 100;
    private TextView textName, textEmail, textPhone, textGender, textBio;
    private Button btnEditProfile, btnLogout, btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageProfileAvatar = findViewById(R.id.image_profile_avatar);
        textName = findViewById(R.id.text_profile_name);
        textEmail = findViewById(R.id.text_profile_email);
        textPhone = findViewById(R.id.text_profile_sdt);
        textGender = findViewById(R.id.text_profile_gender);
        textBio = findViewById(R.id.text_profile_bio);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.back_button);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserInfo();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, screen2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        btnEditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        });

        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, fragmentsContainer.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Người dùng vừa chỉnh sửa xong, tải lại thông tin
            loadUserInfo();
        }
    }

    private void loadUserInfo() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);

                    textName.setText((firstName != null ? firstName : ""));
                    textEmail.setText(lastName != null ? lastName : "");
                    textPhone.setText(phone != null ? phone : "");
                    textGender.setText(gender != null ? gender : "");
                    textBio.setText(bio != null ? bio : "");

                } else {
                    Toast.makeText(ProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
