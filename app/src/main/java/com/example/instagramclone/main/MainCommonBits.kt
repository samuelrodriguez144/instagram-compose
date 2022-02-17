package com.example.instagramclone.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IGViewModel
import com.example.instagramclone.R

@Composable
fun NotificationMessage(vm:IGViewModel){
    val notifState = vm.popupNotification.value
    val notifMessage = notifState?.getContentOrNull()
    if(notifMessage != null){
        Toast.makeText(LocalContext.current, notifMessage, Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun CommonProgressSpinner(){
    Row(
        modifier =
        Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically)
    {
        CircularProgressIndicator()
    }
}
@Composable
fun CheckSignedIn(navController: NavController,vm: IGViewModel){
    val alreadyLoggedIn = remember{ mutableStateOf(false) }
    val isSignedIn = vm.isSignedIn.value

    if(isSignedIn && !alreadyLoggedIn.value){
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.MyPosts.route){
            popUpTo(0)
        }
    }
}

fun navigateTo(navController: NavController,dest: DestinationScreen){
    navController.navigate(dest.route){
        popUpTo(dest.route)
        launchSingleTop = true
    }

}
@OptIn(ExperimentalCoilApi::class)
@Composable
fun CommonImage(
    data:String?,
    modifier:Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop){

    val painter = rememberImagePainter(data = data)
    Image(painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale)

    if(painter.state is ImagePainter.State.Loading){
        CommonProgressSpinner()
    }
}

@Composable
fun UserImageCard(userImage:String?,
                  modifier: Modifier = Modifier
                      .padding(8.dp)
                      .size(64.dp)){
    Card(shape = CircleShape, modifier = modifier) {
        if(userImage.isNullOrEmpty()){
            Image(painter = painterResource(id = R.drawable.ic_user),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray)
            )
        }
        else{
            CommonImage(data = userImage)
        }
    }

}
