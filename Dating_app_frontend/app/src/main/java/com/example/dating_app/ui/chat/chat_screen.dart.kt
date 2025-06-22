package com.example.dating_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dating_app.R
import com.example.dating_app.model.ChatViewModel
import com.example.dating_app.network.UserResponse
import com.example.dating_app.utils.SocketManager
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

data class MessageItem(
    val senderId: String,
    val receiverId: String,
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    viewModel: ChatViewModel = ChatViewModel(userId)
) {
    val connections by viewModel.connections.collectAsState()
    var selectedUser by remember { mutableStateOf<UserResponse?>(null) }
    var messages by remember { mutableStateOf<List<MessageItem>>(emptyList()) }
    var messageInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val socket: Socket? = remember {
        SocketManager.initSocket().also { SocketManager.connect() }
        SocketManager.getSocket()
    }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedUser?._id) {
        selectedUser?.let { chatUser ->
            socket?.off("messageHistory")
            socket?.off("receiveMessage")
            socket?.emit("join", userId)
            socket?.emit("join", chatUser._id)

            val payload = JSONObject().apply {
                put("userId", userId)
                put("otherUserId", chatUser._id)
            }
            socket?.emit("getMessages", payload)

            socket?.on("messageHistory") { args ->
                val rawArray = args.getOrNull(0) as? JSONArray ?: return@on
                val newMessages = mutableListOf<MessageItem>()
                for (i in 0 until rawArray.length()) {
                    val msgObj = rawArray.getJSONObject(i)
                    newMessages.add(
                        MessageItem(
                            senderId = msgObj.getString("senderId"),
                            receiverId = msgObj.getString("receiverId"),
                            message = msgObj.getString("message")
                        )
                    )
                }
                messages = newMessages

                // Now listen for new messages AFTER loading history
                socket?.on("receiveMessage") { msgArgs ->
                    val data = msgArgs.getOrNull(0) as? JSONObject ?: return@on
                    val newMessage = MessageItem(
                        senderId = data.getString("senderId"),
                        receiverId = data.getString("receiverId"),
                        message = data.getString("message")
                    )
                    // Only add if not already present
                    if (newMessage !in messages) {
                        messages = messages + newMessage
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chats", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Chicago, IL", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    if (selectedUser != null) {
                        IconButton(onClick = { selectedUser = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_manage),
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (selectedUser == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(connections) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUser = user
                                    messages = emptyList()
                                    messageInput = ""
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(user.email, fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                        Divider(modifier = Modifier.padding(start = 80.dp, end = 16.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2F2F2))
                ) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Start the conversation!", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            reverseLayout = false
                        ) {
                            items(messages) { msg ->
                                val isSender = msg.senderId == userId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSender) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        tonalElevation = 1.dp,
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            text = msg.message,
                                            color = if (isSender) Color.White else Color.Black,
                                            modifier = Modifier.padding(12.dp),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF0F0F0)),
                            placeholder = { Text("Type a message...") },
                            singleLine = false,
                            maxLines = 4,
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val text = messageInput.trim()
                                if (text.isNotEmpty() && selectedUser != null) {
                                    val msgObj = JSONObject().apply {
                                        put("senderId", userId)
                                        put("receiverId", selectedUser!!._id)
                                        put("message", text)
                                    }
                                    socket?.emit("sendMessage", msgObj)

                                    // Immediately add to list to show
                                    messages = messages + MessageItem(
                                        senderId = userId,
                                        receiverId = selectedUser!!._id,
                                        message = text
                                    )

                                    messageInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
