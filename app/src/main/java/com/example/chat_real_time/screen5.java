package com.example.chat_real_time;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class screen5 extends ScreenshotDetectionActivity {
    ImageView backButton, makeCall, cameraImage;
    RecyclerView recyclerView;
    screen5RVAdaptor adaptor;
    List<com.example.chat_real_time.message> messageList;
    TextView name, onlineStatus;
    EditText message;
    Bitmap image = null;
    FirebaseDatabase database;
    DatabaseReference myRef, myRef1;
    String receiverID;
    FirebaseAuth mAuth;
    users receiverAccount;
    boolean minimized = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen5);

        backButton = findViewById(R.id.back_button);
        makeCall = findViewById(R.id.make_call);
        recyclerView = findViewById(R.id.rv_messages);
        name = findViewById(R.id.name);
        onlineStatus = findViewById(R.id.online_status);
        message = findViewById(R.id.message);
        cameraImage = findViewById(R.id.camera_image);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Messages");
        myRef.keepSynced(true);
        myRef1 = database.getReference("users");
        myRef1.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        messageList = new ArrayList<>();
        adaptor = new screen5RVAdaptor(screen5.this, messageList);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        receiverID = intent.getStringExtra("receiverID");
        image = (Bitmap) intent.getExtras().get("image");
        if (image != null) {
        }

        myRef1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                users firebaseAccount = snapshot.getValue(users.class);
                if (firebaseAccount.getID().equals(receiverID)) {
                    receiverAccount = firebaseAccount;
                    adaptor.setReceiverDP(firebaseAccount.getDp());

                    if (receiverAccount.getState().equals("online")) {
                        onlineStatus.setText("Online now");
                    } else {
                        onlineStatus.setText("Last seen on " + receiverAccount.getLastSeenDate() + " " + receiverAccount.getLastSeenTime());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                users firebaseAccount = snapshot.getValue(users.class);

                if (firebaseAccount.getID().equals(receiverID)) {
                    receiverAccount = firebaseAccount;

                    if (receiverAccount.getState().equals("online")) {
                        onlineStatus.setText("Online now");
                    } else {
                        onlineStatus.setText("Last seen on " + receiverAccount.getLastSeenDate() + " " + receiverAccount.getLastSeenTime());
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                com.example.chat_real_time.message firebaseMessage = snapshot.getValue(com.example.chat_real_time.message.class);
                if ((firebaseMessage.getSenderID().equals(mAuth.getUid())
                        && firebaseMessage.getReceiverID().equals(receiverID)) ||
                        (firebaseMessage.getReceiverID().equals(mAuth.getUid())
                                && firebaseMessage.getSenderID().equals(receiverID))) {
                    messageList.add(firebaseMessage);
                    adaptor.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(messageList.size());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                message msg = snapshot.getValue(message.class);
                for (int i = 0; i < messageList.size(); ++i) {
                    if (messageList.get(i).getKey().equals(msg.getKey())) {
                        messageList.get(i).setMessage(msg.getMessage());
                        adaptor.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < messageList.size(); ++i) {
                    if (messageList.get(i).getKey().equals(snapshot.getValue(message.class).getKey())) {
                        messageList.remove(i);
                        adaptor.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minimized = false;
                finish();
            }
        });
        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(screen5.this, screen11.class);
                intent.putExtra("name", name.getText().toString());
                minimized = false;
                startActivity(intent);
            }
        });
        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    OffsetTime offsetTime = OffsetTime.now();
                    DatabaseReference childReference = myRef.push();
                    childReference.setValue(new message(message.getText().toString(),
                            String.valueOf(offsetTime.getHour() + ":" + offsetTime.getMinute()),
                            "", childReference.getKey(), receiverID, mAuth.getUid(), null));
                    recyclerView.smoothScrollToPosition(messageList.size());
                    message.setText("");
                    return true;
                }
                return false;
            }
        });
        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(screen5.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adaptor);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(70));
        recyclerView.scrollToPosition(messageList.size() - 1);

        updateUserStatus("online");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); ++i) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        image = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    image = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onScreenCaptured(String path) {
        Toast.makeText(this, "Screenshot taken", Toast.LENGTH_SHORT).show();
        File imgFile = new File(path);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            image = myBitmap;
            OffsetTime offsetTime = OffsetTime.now();
            DatabaseReference childReference = myRef.push();
            childReference.setValue(new message("User took a screenshot",
                    String.valueOf(offsetTime.getHour() + ":" + offsetTime.getMinute()),
                    "", childReference.getKey(), receiverID, mAuth.getUid(), null));
            recyclerView.smoothScrollToPosition(messageList.size());
        }
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Làm ơn ", Toast.LENGTH_SHORT).show();
    }

    private void updateUserStatus(String state) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        reference.child(auth.getUid()).child("state").setValue(state);
        reference.child(auth.getUid()).child("lastSeenTime").setValue(saveCurrentTime);
        reference.child(auth.getUid()).child("lastSeenDate").setValue(saveCurrentDate);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        minimized = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUserStatus("online");
        minimized = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserStatus("online");
        minimized = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (minimized) {
            updateUserStatus("offline");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (minimized) {
            updateUserStatus("offline");
        }
    }
}