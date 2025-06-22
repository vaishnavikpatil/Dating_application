package com.example.dating_app.ui.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dating_app.R
import com.example.dating_app.data.UserPreferences
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.dating_app.network.UserResponse
import com.example.dating_app.network.ConnectionRequestBody
import com.example.dating_app.network.RetrofitClient

data class UserCard(val name: String, val age: Int, val bio: String)





@Composable
fun SwipeScreen() {
    val context = LocalContext.current
    val userPrefs = UserPreferences(context)
    val currentUserId = userPrefs.getUserId()

    if (currentUserId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not logged in", fontSize = 20.sp)
        }
        return
    }

    val api = RetrofitClient.api
    val scope = rememberCoroutineScope()

    var cards by remember { mutableStateOf(listOf<UserResponse>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Load all users except current user
            cards = api.getAllUsers().filter { it._id != currentUserId }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load users"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top App Bar mimic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_media_previous),
                    contentDescription = "Back"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Discover", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Chicago, IL", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_manage),
                    contentDescription = "Filter"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = errorMessage ?: "Error", color = Color.Red, fontSize = 18.sp)
            }
        } else if (cards.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No more profiles", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                cards.forEachIndexed { index, user ->
                    val isTop = index == cards.lastIndex
                    val offsetY = 8.dp * (cards.size - 1 - index)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .padding(top = offsetY)
                    ) {
                        if (isTop) {
                            SwipeableCard(
                                user = user,
                                onSwipedLeft = {
                                    cards = cards.toMutableList().also { it.remove(user) }
                                },
                                onSwipedRight = {
                                    cards = cards.toMutableList().also { it.remove(user) }

                                    // Send connection request
                                    scope.launch {
                                        try {
                                            api.sendRequest(
                                                ConnectionRequestBody(
                                                    senderId = currentUserId,
                                                    receiverId = user._id
                                                )
                                            )
                                        } catch (e: Exception) {
                                            // Handle error (could show snackbar or log)
                                        }
                                    }
                                }
                            )
                        } else {
                            UserProfileCardFromApi(user = user)
                        }
                    }
                }
            }
        }

        // Buttons below the card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (cards.isNotEmpty()) cards = cards.dropLast(1)
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_delete),
                    contentDescription = "Dislike",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    if (cards.isNotEmpty()) {
                        val likedUser = cards.last()
                        cards = cards.dropLast(1)

                        // Send connection request on like button click
                        scope.launch {
                            try {
                                api.sendRequest(
                                    ConnectionRequestBody(
                                        senderId = currentUserId,
                                        receiverId = likedUser._id
                                    )
                                )
                            } catch (e: Exception) {
                                // Handle error if needed
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SwipeableCard(
    user: UserResponse,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX.value > 300f -> {
                                scope.launch {
                                    offsetX.animateTo(1000f, tween(300))
                                    onSwipedRight()
                                }
                            }

                            offsetX.value < -300f -> {
                                scope.launch {
                                    offsetX.animateTo(-1000f, tween(300))
                                    onSwipedLeft()
                                }
                            }

                            else -> {
                                scope.launch {
                                    offsetX.animateTo(0f, tween(300))
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    }
                )
            }
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .rotate(offsetX.value / 20)
    ) {
        UserProfileCardFromApi(user = user)
    }
}

@Composable
fun UserProfileCardFromApi(user: UserResponse) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.girl2), // Replace with your actual image or load dynamically if available
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        user.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        user.email,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
