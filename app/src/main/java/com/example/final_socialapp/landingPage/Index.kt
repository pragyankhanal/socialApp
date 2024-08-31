package com.example.final_socialapp.landingPage

import com.example.final_socialapp.database.PostDao
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.final_socialapp.LoginActivity
import com.example.final_socialapp.database.AppDatabase
import com.example.final_socialapp.database.Post
import com.example.final_socialapp.database.User
import com.example.final_socialapp.database.UserDao
import com.example.final_socialapp.ui.theme.Final_socialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IndexActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
        val userDao = db.userDao()
        val postDao = db.postDao()

        // Get the username from the intent extras
        val username = intent.getStringExtra("USERNAME") ?: ""

        setContent {
            Final_socialAppTheme {
                IndexScreen(username, userDao, postDao)
            }
        }
    }
}

@Composable
fun IndexScreen(username: String, userDao: UserDao, postDao: PostDao) {
    var user by remember { mutableStateOf<User?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<Post?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch user data and posts
    LaunchedEffect(username) {
        userDao.getUserByUsername(username)?.let {
            user = it
        }
        posts = postDao.getAllPosts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                user?.let { currentUser ->
                    // Circular icon and welcome message
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentUser.firstName.first().uppercaseChar().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Welcome, ${currentUser.firstName}!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                } ?: run {
                    Text(
                        text = "Welcome!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                LogoutButton()
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space between welcome message and posts

            // Posts displayed in LazyColumn
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        isOwner = post.username == username,
                        onEdit = {
                            editingPost = post
                            showDialog = true
                        },
                        onDelete = {
                            postToDelete = post // Set the post to be deleted
                            showDeleteConfirmationDialog = true // Show the confirmation dialog
                        }
                    )
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    editingPost = null // Reset for new post
                    showDialog = true
                },
                modifier = Modifier.size(56.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }

        // Post dialog handling
        if (showDialog) {
            PostDialog(
                onDismiss = {
                    showDialog = false
                    editingPost = null // Reset after dismiss
                    coroutineScope.launch {
                        posts = postDao.getAllPosts() // Refresh posts after dismiss
                    }
                },
                username = user?.username ?: "",
                postDao = postDao,
                post = editingPost // Pass the post to be edited
            )
        }

        // Delete confirmation dialog
        if (showDeleteConfirmationDialog && postToDelete != null) {
            DeleteConfirmationDialog(
                post = postToDelete!!,
                onConfirm = {
                    coroutineScope.launch {
                        postDao.deletePost(postToDelete!!)
                        posts = postDao.getAllPosts() // Refresh posts after deletion
                    }
                    showDeleteConfirmationDialog = false // Hide dialog
                    postToDelete = null // Reset
                },
                onDismiss = {
                    showDeleteConfirmationDialog = false // Hide dialog
                    postToDelete = null // Reset
                }
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .heightIn(min = 120.dp), // Ensure a minimum height for all posts
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with username and three dots menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Space between username and menu
                ) {
                    Text(text = post.username, style = MaterialTheme.typography.titleSmall)

                    if (isOwner) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
            }

            // Dropdown menu positioned below the three dots button
            if (isOwner && expanded) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = 240.dp, y = -40.dp) // Adjust vertical offset to position below the button
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Post") },
                        onClick = {
                            onEdit() // Handle edit action
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Post") },
                        onClick = {
                            onDelete() // Handle delete action
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutButton() {
    val context = LocalContext.current
    IconButton(onClick = {
        // Handle logout action
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish() // Close the current activity
    }) {
        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
    }
}

@Composable
fun PostDialog(onDismiss: () -> Unit, username: String, postDao: PostDao, post: Post?) {
    var postContent by remember { mutableStateOf(post?.content ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (post == null) "Create Post" else "Edit Post") },
        text = {
            Column {
                TextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    label = { Text("Post Content") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Handle creating or updating post
                    if (post == null) {
                        // Create new post
                        val newPost = Post(username = username, content = postContent, userId = username) // Pass userId appropriately
                        CoroutineScope(Dispatchers.IO).launch {
                            postDao.insertPost(newPost)
                        }
                    } else {
                        // Update existing post
                        CoroutineScope(Dispatchers.IO).launch {
                            postDao.updatePost(post.copy(content = postContent))
                        }
                    }
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(post: Post, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete this post?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
