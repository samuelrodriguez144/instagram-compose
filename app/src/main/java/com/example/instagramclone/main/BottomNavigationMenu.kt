package com.example.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.R
import com.example.instagramclone.ui.theme.localColor
import com.example.instagramclone.ui.theme.spacing


enum class BottomNavigationMenuItem (val icon: Int,val navDestination: DestinationScreen){
    FEED(R.drawable.ic_home,DestinationScreen.Feed),
    SEARCH(R.drawable.ic_search,DestinationScreen.Search),
    POST(R.drawable.ic_posts,DestinationScreen.MyPosts)
}


@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationMenuItem,navController: NavController){

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight().graphicsLayer {
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp
            )
            clip = true
        }
        .background(MaterialTheme.colors.primary)) {
        for(item in BottomNavigationMenuItem.values()) {
            Image(painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .padding(MaterialTheme.spacing.small)
                    .weight(1f)
                    .clickable {
                    navigateTo(navController,item.navDestination)
                },
            colorFilter = if(item == selectedItem) ColorFilter.tint(Color.Black) else ColorFilter.tint(Color.White),
            )
        }
    }

}