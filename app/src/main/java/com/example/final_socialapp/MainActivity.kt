package com.example.final_socialapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.final_socialapp.ui.theme.Final_socialAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Final_socialAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSignUpClick = { navigateToSignUp() },
                        onLoginClick = { navigateToLogin() }
                    )
                }
            }
        }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onSignUpClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Sign Up")
        }
        Button(onClick = onLoginClick, modifier = Modifier.padding(8.dp)) {
            Text(text = "Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Final_socialAppTheme {
        MainScreen(onSignUpClick = {}, onLoginClick = {})
    }
}
