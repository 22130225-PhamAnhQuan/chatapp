package com.example.chat_real_time;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class fragment_screen6 extends Fragment {
    RecyclerView recyclerView;
    screen6RVAdaptor adaptor;
    List<contact> contactList;
    RelativeLayout newContact, newGroup;
    List<users> accounts;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    CircleImageView dp;
    TextView name;

    ValueEventListener userValueEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen6, container, false);

        recyclerView = view.findViewById(R.id.rv_contacts);
        newContact = view.findViewById(R.id.rl_new_contact);
        newGroup = view.findViewById(R.id.rl_new_group);
        dp = view.findViewById(R.id.dp);
        name = view.findViewById(R.id.name);

        contactList = new ArrayList<>();
        accounts = new ArrayList<>();

        myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        adaptor = new screen6RVAdaptor(getActivity(), contactList, fragment_screen6.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adaptor);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(50));

        // Load user list from Firebase once
        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;  // Fragment đã detach, không xử lý
                accounts.clear();
                contactList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    users user = dataSnapshot.getValue(users.class);
                    if (user != null) {
                        accounts.add(user);
                        // Nếu là user hiện tại thì cập nhật thông tin UI
                        if (user.getID() != null && user.getID().equals(mAuth.getUid())) {
                            if (getActivity() != null) {
                                name.setText(user.getFirstName() + " " + user.getLastName());
                                Picasso.get().load(user.getDp()).into(dp);
                            }
                        }
                    }
                }
                // Sau khi có danh sách users, load contacts điện thoại
                addPhoneContactsToList();
                adaptor.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Không tải được người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        myRef.addValueEventListener(userValueEventListener);

        newContact.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Liên hệ mới", Toast.LENGTH_LONG).show());

        newGroup.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Clicked on new group", Toast.LENGTH_LONG).show());

        return view;
    }

    private void addPhoneContactsToList() {
        if (getActivity() == null) return;
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        if (phones == null) return;

        try {
            while (phones.moveToNext()) {
                int index = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNo = phones.getString(index);

                if (phoneNo == null) continue;

                // Chuẩn hóa số điện thoại, ví dụ: +92 thành 0
                phoneNo = phoneNo.replace("+92", "0");
                if (phoneNo.length() > 4 && phoneNo.charAt(4) == ' ') {
                    StringBuilder stringBuilder = new StringBuilder(phoneNo);
                    stringBuilder.deleteCharAt(4);
                    phoneNo = stringBuilder.toString();
                }

                for (users account : accounts) {
                    String accountPhone = account.getPhoneNumber();
                    String accountID = account.getID();
                    String currentUserID = mAuth.getUid();

                    if (accountPhone != null && accountPhone.equals(phoneNo)
                            && accountID != null && !accountID.equals(currentUserID)) {
                        // Tránh thêm trùng
                        boolean exists = false;
                        for (contact c : contactList) {
                            if (c.getDp().equals(phoneNo)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            contactList.add(new contact(account.getFirstName() + " " + account.getLastName(),
                                    phoneNo, account.getDp()));
                        }
                        break;
                    }
                }
            }
        } finally {
            phones.close();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myRef != null && userValueEventListener != null) {
            myRef.removeEventListener(userValueEventListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof fragmentsContainer) {
            ((fragmentsContainer) getActivity()).changeImageColorToBlue(1);
        }
    }

    public void applicationNotMinimized() {
        if (getActivity() instanceof fragmentsContainer) {
            ((fragmentsContainer) getActivity()).minimized = false;
        }
    }
}
