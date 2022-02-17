package com.example.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.R


enum class BottomNavigationMenuItem (val icon: Int,val navDestination: DestinationScreen){
    FEED(R.drawable.ic_home,DestinationScreen.Feed),
    SEARCH(R.drawable.ic_search,DestinationScreen.Search),
    POST(R.drawable.ic_posts,DestinationScreen.MyPosts)
}


@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationMenuItem,navController: NavController){

    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(top = 4.dp)
        .background(Color.White)) {
        for(item in BottomNavigationMenuItem.values()) {
            Image(painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp).padding(5.dp).weight(1f).clickable {
                    navigateTo(navController,item.navDestination)
                },
            colorFilter = if(item == selectedItem) ColorFilter.tint(Color.Gray) else ColorFilter.tint(Color.Black))
        }
    }

}