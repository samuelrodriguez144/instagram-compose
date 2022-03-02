package com.example.instagramclone.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IGViewModel
import com.example.instagramclone.R



@Composable
fun MyPostScreen(navController: NavController,vm:IGViewModel){

    val newPostImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){ uri ->
        uri?.let {
            val encodedImage = Uri.encode(it.toString())
            val route = DestinationScreen.NewPost.createRoute(encodedImage)
            navController.navigate(route)
        }

    }
    val userData = vm.userData.value
    val isLoading = vm.inProgress.value
    
    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row() {
                ProfileImage(userData?.imageUrl){
                    newPostImageLauncher.launch("image/*")
                }
                Text(text = "15\nPosts",
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = CenterVertically),
                    textAlign = TextAlign.Center)
                Text(text = "15\nFollowers",
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = CenterVertically),
                    textAlign = TextAlign.Center)
                Text(text = "15\nFollowing",
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = CenterVertically),
                    textAlign = TextAlign.Center)
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val userNameDisplay = if(userData?.username == null ) "" else "@${userData.username}"
                Text(text = userData?.name ?: "" , fontWeight = FontWeight.Bold)
                Text(text = userNameDisplay)
                Text(text = userData?.bio ?: "")
            }

            OutlinedButton(onClick = {
                navigateTo(navController = navController, dest = DestinationScreen.MyProfile)
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            shape = RoundedCornerShape(10)) {
                Text(text = "Edit Profile", color = Color.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Posts List")
            }

        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationMenuItem.POST,
            navController = navController)

        if(isLoading){
            CommonProgressSpinner()
        }
    }
}


@Composable
fun ProfileImage(imageUrl:String?, onclick: ()->Unit){
    
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onclick.invoke() }) {

        UserImageCard(
            userImage = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp))

        Card(shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)) {
            
            Image(painter = painterResource(id = R.drawable.ic_plus), contentDescription =null, modifier = Modifier
                .background(Color.Black)
                 )
        }
    }

}