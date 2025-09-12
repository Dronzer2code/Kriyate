package com.example.cityconnect

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoginScreen by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var showVerificationMessage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Main container with background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GovColors.LightGray)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showVerificationMessage) {
            // --- UI to show after successful registration ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(GovColors.Success.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MarkEmailRead,
                            contentDescription = "Email Sent",
                            modifier = Modifier.size(48.dp),
                            tint = GovColors.Success
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Verify Your Email",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovColors.NavyBlue,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "A verification link has been sent to your email address. Please check your inbox and click the link to activate your account.",
                        textAlign = TextAlign.Center,
                        color = GovColors.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showVerificationMessage = false
                            isLoginScreen = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovColors.NavyBlue)
                    ) {
                        Text("Return to Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // --- Login/Register UI ---
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (isLoginScreen) "Citizen Login" else "Create Account",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovColors.NavyBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isLoginScreen) "Sign in to access the portal." else "Complete the form to register.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GovColors.DarkGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(0.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GovColors.AccentBlue,
                                focusedLabelColor = GovColors.AccentBlue,
                                cursorColor = GovColors.AccentBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(0.dp),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                val description = if (passwordVisible) "Hide password" else "Show password"
                                IconButton(onClick = {passwordVisible = !passwordVisible}){
                                    Icon(imageVector  = image, description)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GovColors.AccentBlue,
                                focusedLabelColor = GovColors.AccentBlue,
                                cursorColor = GovColors.AccentBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                isLoading = true
                                if (isLoginScreen) {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            } else {
                                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                task.result?.user?.sendEmailVerification()
                                                    ?.addOnCompleteListener { verificationTask ->
                                                        isLoading = false
                                                        if(verificationTask.isSuccessful) {
                                                            showVerificationMessage = true
                                                        } else {
                                                            Toast.makeText(context, "Failed to send verification email.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            },
                            enabled = !isLoading && email.isNotBlank() && password.length >= 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovColors.NavyBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(
                                    if (isLoginScreen) "Sign In" else "Create Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch between Login/Register
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLoginScreen) "Don't have an account?" else "Already have an account?",
                                color = GovColors.DarkGray,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { isLoginScreen = !isLoginScreen }) {
                                Text(
                                    text = if (isLoginScreen) "Register Now" else "Sign In",
                                    color = GovColors.AccentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
