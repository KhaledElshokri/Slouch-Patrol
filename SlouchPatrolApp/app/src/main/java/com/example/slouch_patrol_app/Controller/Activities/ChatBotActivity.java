package com.example.slouch_patrol_app.Controller.Activities;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.slouch_patrol_app.BuildConfig;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slouch_patrol_app.Features.ChatAdapter;
import com.example.slouch_patrol_app.Features.Message;
import com.example.slouch_patrol_app.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//import pl.droidsonroids.gif.BuildConfig;

public class ChatBotActivity extends AppCompatActivity {


    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private String openAI_key = BuildConfig.OPENAI_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        //set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_bot);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        inputMessage.setHint("Enter your question here");

        messageList.add(new Message("Hello!\n\n I am Slouchy, Here to help you stop slouching!",false));

        // Handle send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = inputMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(userMessage)) {
                    // Add user message to list
                    messageList.add(new Message(userMessage, true));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);

                    inputMessage.setText(""); // Clear input
                    inputMessage.setHint("Enter your question here");

                    // Send message to Gemini API
                    sendMessageToOpenAI(userMessage);
                }
            }
        });
    }

    //allow toolbar to route back to parent activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessageToOpenAI(String message) {
        // Define OpenAI API endpoint
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        // Create the request payload
        JSONObject requestBody = new JSONObject();
        try {
            // Define the required fields
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new JSONArray()
                    .put(new JSONObject()
                            .put("role", "system")
                            .put("content", "You are a helpful assistant."))
                    .put(new JSONObject()
                            .put("role", "user")
                            .put("content", message)));

            // Optional fields for customization
            requestBody.put("temperature", 0.7); // Adjust randomness (0.0 deterministic, 2.0 very random)
            requestBody.put("top_p", 1); // Nucleus sampling
            requestBody.put("n", 1); // Number of responses
            requestBody.put("max_completion_tokens", 300); // Maximum tokens for the response
            requestBody.put("frequency_penalty", 0.0); // Penalize repeated phrases
            requestBody.put("presence_penalty", 0.0); // Encourage new topics
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request payload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define MediaType for JSON
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // Create the HTTP client and request
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + openAI_key) // Replace with your actual API key
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the API call asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OpenAI API Error", "Request failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatBotActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        // Parse the response
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        String botResponse = choices.getJSONObject(0).getJSONObject("message").getString("content");

                        // Update the UI with the bot's response
                        runOnUiThread(() -> {
                            messageList.add(new Message(botResponse, false));
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        });
                    } catch (JSONException e) {
                        Log.e("OpenAI API Error", "JSON parsing error: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(ChatBotActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Log detailed API error information
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("OpenAI API Error", "Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                    runOnUiThread(() -> Toast.makeText(ChatBotActivity.this, "API Error: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }



}
