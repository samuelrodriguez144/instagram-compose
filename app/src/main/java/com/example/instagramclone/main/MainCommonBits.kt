package com.example.instagramclone.main

import android.os.Parcelable
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
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
import com.example.instagramclone.ui.theme.spacing

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
        navController.navigate(DestinationScreen.Feed.route){
            popUpTo(0)
        }
    }
}

data class NavParams(
    val name:String,
    val value:Parcelable
)
fun navigateTo(navController: NavController,dest: DestinationScreen, vararg params:NavParams){
    for (param in params){
        navController.currentBackStackEntry?.arguments?.putParcelable(param.name,param.value)
    }
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
                      .padding(MaterialTheme.spacing.extraSmall)
                      .size(MaterialTheme.spacing.extraLarge)){
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

@Composable
fun CommonDivider(){
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = MaterialTheme.spacing.extraSmall, bottom = MaterialTheme.spacing.extraSmall)
    )
}

private enum class LikeIconSize{
    SMALL,
    LARGE
}

@Composable
fun LikeAnimation(like: Boolean = true){
    var sizeState by remember{ mutableStateOf(LikeIconSize.SMALL)}
    val transition  = updateTransition(targetState = sizeState, label = "")
    val size by transition.animateDp(label = "", transitionSpec = {
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }
    ) { state ->
        when(state){
            LikeIconSize.SMALL -> MaterialTheme.spacing.default
            LikeIconSize.LARGE -> 150.dp
        }

    }
    Image(
        painter = painterResource(id = if(like) R.drawable.ic_like else R.drawable.ic_dislike),
        contentDescription = null,
        modifier = Modifier.size(size = size),
        colorFilter = ColorFilter.tint(if(like) Color.Red else Color.LightGray))

    sizeState = LikeIconSize.LARGE
}
