package com.example.dating_app.ui.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dating_app.data.UserPreferences
import com.example.dating_app.network.ApiService
import com.example.dating_app.network.LoginRequest
import com.example.dating_app.network.RegisterRequest
import com.example.dating_app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val api: ApiService = RetrofitClient.api

    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Welcome Back!" else "Create Account",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Info,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = if (isLoginMode) {
                            api.login(LoginRequest(email, password))
                        } else {
                            api.register(RegisterRequest(name, email, password))
                        }

                        val id = response._id
                        val userName = response.name
                        val userEmail = response.email

                        if (id != null && userName != null && userEmail != null) {
                            userPrefs.saveUser(id, userName, userEmail)

                            launch(Dispatchers.Main) {
                                isLoading = false
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Incomplete user data from server.",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "API call failed", e)
                        launch(Dispatchers.Main) {
                            isLoading = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error: ${e.localizedMessage ?: e.toString()}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (isLoginMode) "Login" else "Register",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { isLoginMode = !isLoginMode },
        ) {
            Text(
                text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
                color = MaterialTheme.colorScheme.primary
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
