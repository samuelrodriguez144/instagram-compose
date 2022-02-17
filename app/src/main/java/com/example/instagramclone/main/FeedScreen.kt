package com.example.instagramclone.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.instagramclone.IGViewModel

@Composable
fun FeedScreen(navController: NavController,vm:IGViewModel){
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Feed Screen")
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationMenuItem.FEED,
            navController = navController)
        
    }
}