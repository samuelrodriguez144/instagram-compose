package com.example.instagramclone.main

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.instagramclone.IGViewModel
import com.example.instagramclone.ui.theme.spacing


@Composable
fun NewPostScreen(navController: NavController,vm:IGViewModel,encodedUri:String){
    val imageUri by remember {
        mutableStateOf(encodedUri)
    }
    var description by rememberSaveable{
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .verticalScroll(scrollState)
        .fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.extraSmall),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Cancel", modifier = Modifier.clickable { navController.popBackStack() })
            Text(text = "Post", modifier = Modifier
                .clickable {
                    focusManager.clearFocus()
                    vm.onNewPost(Uri.parse(imageUri), description = description){
                        navController.popBackStack()
                    }
                })
        }
        CommonDivider()
        Image(painter = rememberImagePainter(imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp),
            contentScale = ContentScale.FillWidth)
        
        Row(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            OutlinedTextField(value = description,
                onValueChange = {description = it},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            label = { Text(text = "Caption")},
            singleLine = false,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = Color.Black
            ))
        }
    }
    val inProgress = vm.inProgress.value

    if(inProgress){
        CommonProgressSpinner()
    }
}

