package com.example.cityconnect

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    // --- 1. ADDED state for the new "Full Name" field ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // --- 2. ADDED state for password visibility toggle ---
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoginScreen by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var showVerificationMessage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    if (showVerificationMessage) {
        // --- UI to show after successful registration ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Email Sent",
                modifier = Modifier.size(64.dp),
                tint = GovColors.Success
            )
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
                color = GovColors.DarkGray
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
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Return to Login", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // --- Original Login/Register UI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isLoginScreen) "Citizen Login" else "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GovColors.NavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (isLoginScreen) "Sign in to continue." else "Create a new account to get started.",
                textAlign = TextAlign.Center,
                color = GovColors.DarkGray
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. ADDED "Full Name" TextField (only visible on registration screen) ---
            if (!isLoginScreen) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(0.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(0.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. UPDATED Password TextField with visibility toggle ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(0.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                        // --- 5. UPDATED Registration logic to include Display Name ---
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Set the user's display name
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    task.result?.user?.updateProfile(profileUpdates)

                                    // Send the verification email
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
                // Updated button validation to check for name on registration screen
                enabled = !isLoading && email.isNotBlank() && password.length >= 6 && (isLoginScreen || name.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(if (isLoginScreen) "Sign In" else "Register", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLoginScreen) "Don't have an account? Register" else "Already have an account? Sign In",
                modifier = Modifier.clickable { isLoginScreen = !isLoginScreen },
                color = GovColors.AccentBlue,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
