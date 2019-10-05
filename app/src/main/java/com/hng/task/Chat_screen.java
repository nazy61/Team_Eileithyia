package com.hng.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hng.task.models.ChatMessage;
import com.hng.task.models.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class Chat_screen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AIListener {

    private final int VOICE_REQUEST_CODE = 100;
    private static final String TAG = "Chat Screen";

    //firebase variables
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference users;

    //widgets
    private DrawerLayout mDrawerLayout;
    private ImageView mBack;
    private EditText txtMessage;
    private ImageView sendMessage;
    private ImageView sendVoiceMessage;
    private TextView name;
    private ImageView profilePicture;

    //utilities
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private List<ChatMessage> mChatMessages;
    private String g;


    DatabaseReference ref;
    FirebaseRecyclerAdapter<ChatMessage,chat_rec> adapter;
    private AIService aiService;
    private AIDataService mAiDataService;
    private AIRequest mAiRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        //init firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        g = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "onCreate: " + g);
        users = mFirebaseDatabase.getReference("users").child(g);

        //widgets
        txtMessage = findViewById(R.id.txt_message);
        txtMessage.setMovementMethod(new ScrollingMovementMethod());
        sendMessage = findViewById(R.id.send_message);
        sendVoiceMessage = findViewById(R.id.send_voice_message);

        //NavigationView Widgets
        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        mBack = headerView.findViewById(R.id.hide_sidebar);
        name = headerView.findViewById(R.id.header_title);
        profilePicture = headerView.findViewById(R.id.header_icon);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //utilities
        mChatMessages = new ArrayList<>();
        mRecyclerView = findViewById(R.id.chat_list);
        mLinearLayoutManager = new LinearLayoutManager(this);


        //attach utilities
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);

        //CHAT BOT LOGIC INITIALIZER
        final AIConfiguration config = new AIConfiguration("6f109cdda871424f85dbb5398f6f6586",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        mAiDataService = new AIDataService(config);

        mAiRequest = new AIRequest();


//        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, mChatMessages);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);

        //listeners
        setNavigationViewListener();

        sendVoiceMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, VOICE_REQUEST_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToBot(mAiRequest, mAiDataService);

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class,R.layout.message_list,chat_rec.class,ref.child("chat").child(mAuth.getCurrentUser().getUid())) {
            @Override
            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {

                if (model.getSender().equals("user")) {


                    viewHolder.userText.setText(model.getMessage());

                    viewHolder.userText.setVisibility(View.VISIBLE);
                    viewHolder.chatbotText.setVisibility(View.GONE);
                }
                else {
                    viewHolder.chatbotText.setText(model.getMessage());

                    viewHolder.userText.setVisibility(View.GONE);
                    viewHolder.chatbotText.setVisibility(View.VISIBLE);
                }
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);

                }

            }
        });


        mRecyclerView.setAdapter(adapter);

    }

    private void sendMessageToBot(final AIRequest aiRequest, final AIDataService aiDataService) {
        String message = txtMessage.getText().toString();
                /*ChatMessage botMessage = new ChatMessage(message, "chatBot");
                mChatMessages.add(userMessage);
                mChatMessages.add(botMessage);
                txtMessage.setText("");
                mChatRecyclerViewAdapter.notifyDataSetChanged();*/

        if (!message.equals("")) {
            ChatMessage userMessage = new ChatMessage(message, "user");
            mChatMessages.add(userMessage);
            ref.child("chat").child(mAuth.getCurrentUser().getUid()).push().setValue(userMessage);

            aiRequest.setQuery(message);
            new AsyncTask<AIRequest,Void, AIResponse>(){

                @Override
                protected AIResponse doInBackground(AIRequest... aiRequests) {
                    final AIRequest request = aiRequests[0];
                    try {
                        final AIResponse response = aiDataService.request(aiRequest);
                        return response;
                    } catch (AIServiceException e) {
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(AIResponse response) {
                    if (response != null) {

                        Result result = response.getResult();
                        String reply = result.getFulfillment().getSpeech();
                        ChatMessage botMessage = new ChatMessage(reply, "chatbot");
                        mChatMessages.add(botMessage);
                        ref.child("chat").child(mAuth.getCurrentUser().getUid()).push().setValue(botMessage);
                    }
                }
            }.execute(aiRequest);
        }
        else {
            aiService.startListening();
        }
//                int newMsgPosition = mChatMessages.size() - 1;
//                adapter.notifyDataSetChanged();
//                mRecyclerView.smoothScrollToPosition(newMsgPosition);

        mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView,new RecyclerView.State(), mRecyclerView.getAdapter().getItemCount());
        txtMessage.setText("");
        try  {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    private void chatbotAI(){

    }

    private void setNavigationViewListener() {
        Log.d(TAG, "setNavigationViewListener: initializing navigation drawer listener");
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.my_profile:
                Intent intent = new Intent(Chat_screen.this, Profile.class);
                startActivity(intent);
                return true;
            case R.id.log_out:
                if(mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    Intent mine = new Intent(Chat_screen.this, Sign_in.class);
                    Toast.makeText(Chat_screen.this, "Signed out", Toast.LENGTH_LONG).show();
                    startActivity(mine);
                    finish();
                }
                return true;
            case R.id.share:
                Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setText("https://drive.google.com/open?id=1CbBSD_y3e1PGjJnGLawq9LlqF6UuN02B")
                        .getIntent();
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
                return true;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onResult(AIResponse result) {
        Result responseResult = result.getResult();

        String message = responseResult.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        ref.child("chat").child(mAuth.getCurrentUser().getUid()).push().setValue(chatMessage0);


        String reply = responseResult.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        ref.child("chat").child(mAuth.getCurrentUser().getUid()).push().setValue(chatMessage);
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    protected void onStart() {
        super.onStart();

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    User user = dataSnapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: " + user.getFullName());
                    name.setText(user.getFullName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(g + "/image");

        try {
            final File localFile = File.createTempFile("pic", "jpg");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    profilePicture.setImageURI(Uri.fromFile(localFile));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Chat_screen.this, "Please Set Your Profile Image from the profile section", Toast.LENGTH_LONG).show();
                    return;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VOICE_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtMessage.setText(result.get(0).toString());
                    sendMessageToBot(mAiRequest, mAiDataService);
                }
                break;
            }
        }
    }
}
