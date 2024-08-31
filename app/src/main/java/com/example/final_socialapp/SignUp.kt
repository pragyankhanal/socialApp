package com.example.final_socialapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.final_socialapp.database.AppDatabase
import com.example.final_socialapp.database.User
import com.example.final_socialapp.database.UserDao
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
        val userDao = db.userDao()

        setContent {
            SignUpScreen(userDao) { firstName, lastName, email, username, password ->
                lifecycleScope.launch {
                    val user = User(
                        username = username,
                        password = password,
                        email = email,
                        lastName = lastName,
                        firstName = firstName
                    )
                    userDao.insertUser(user)
                }
            }
        }
    }
}


@Composable
fun SignUpScreen(userDao: UserDao, onSignUp: (String, String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = if (it.isNotEmpty() && !it.first().isUpperCase()) {
                        "First letter must be capitalized"
                    } else {
                        ""
                    }
                },
                label = { Text("First Name") },
                isError = firstNameError.isNotEmpty()
            )
            if (firstNameError.isNotEmpty()) {
                Text(firstNameError, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = if (it.isNotEmpty() && !it.first().isUpperCase()) {
                        "First letter must be capitalized"
                    } else {
                        ""
                    }
                },
                label = { Text("Last Name") },
                isError = lastNameError.isNotEmpty()
            )
            if (lastNameError.isNotEmpty()) {
                Text(lastNameError, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (it.isNotEmpty() && !it.contains("@")) {
                        "Enter a valid email"
                    } else {
                        ""
                    }
                },
                label = { Text("Email") },
                isError = emailError.isNotEmpty()
            )
            if (emailError.isNotEmpty()) {
                Text(emailError, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (it.isNotEmpty()) {
                        coroutineScope.launch {
                            val existingUser = userDao.getUser(it) // Adjust this method as needed
                            usernameError = if (existingUser != null) {
                                "Username already exists"
                            } else {
                                ""
                            }
                        }
                    } else {
                        usernameError = ""
                    }
                },
                label = { Text("Username") },
                isError = usernameError.isNotEmpty()
            )
            if (usernameError.isNotEmpty()) {
                Text(usernameError, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.length < 8) {
                        "Password must be at least 8 characters long"
                    } else {
                        ""
                    }
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon: ImageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                isError = passwordError.isNotEmpty()
            )
            if (passwordError.isNotEmpty()) {
                Text(passwordError, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) {
                        "Passwords do not match"
                    } else {
                        ""
                    }
                },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon: ImageVector = if (confirmPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                isError = confirmPasswordError.isNotEmpty()
            )
            if (confirmPasswordError.isNotEmpty()) {
                Text(confirmPasswordError, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (password == confirmPassword && firstNameError.isEmpty() && lastNameError.isEmpty() &&
                        emailError.isEmpty() && usernameError.isEmpty() && passwordError.isEmpty() &&
                        confirmPasswordError.isEmpty()) {
                        onSignUp(firstName, lastName, email, username, password)
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Sign Up")
            }
        }
    }
}

