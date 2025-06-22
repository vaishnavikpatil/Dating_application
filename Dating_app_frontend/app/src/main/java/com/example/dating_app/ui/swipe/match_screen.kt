package com.example.dating_app.ui.match

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dating_app.R
import com.example.dating_app.data.UserPreferences
import com.example.dating_app.network.ConnectionRequestResponse
import com.example.dating_app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MatchScreen() {
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    val userId = prefs.getUserId()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please login first", fontSize = 20.sp)
        }
        return
    }

    val scope = rememberCoroutineScope()
    val api = RetrofitClient.api

    var requests by remember { mutableStateOf<List<ConnectionRequestResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            requests = api.getPendingRequests(userId)
            isLoading = false
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Failed to load matches"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Matches", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Chicago, IL", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_manage),
                    contentDescription = "Settings"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(text = error!!, color = Color.Red)
            }
            requests.isEmpty() -> {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No pending requests.", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }
            else -> {
                Column(modifier = Modifier.weight(1f)) {
                    requests.forEach { request ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Gray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.girl2),
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = request.senderId.name,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = request.senderId._id,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                api.acceptRequest(request._id)
                                                requests = requests - request
                                            } catch (e: Exception) {
                                                error = "Failed to accept request"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Accept", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
