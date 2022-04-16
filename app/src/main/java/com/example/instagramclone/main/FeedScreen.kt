package com.example.instagramclone.main

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IGViewModel
import com.example.instagramclone.data.PostData
import com.example.instagramclone.ui.theme.animation.ShimmerAnimation
import com.example.instagramclone.ui.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(navController: NavController,vm:IGViewModel){

    val userDataLoading = vm.inProgress.value
    val userData = vm.userData.value
    val personalizedFeed = vm.postFeed.value
    val personalizedFeedLoading = vm.postFeedProgress.value

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)){
            UserImageCard(userImage = userData?.imageUrl)
        }
        PostLists(
            posts = personalizedFeed,
            modifier = Modifier.weight(1f).background(Color.White),
            loading = personalizedFeedLoading or userDataLoading,
            navController = navController,
            vm = vm,
            currentUserId = userData?.userId ?: ""
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationMenuItem.FEED,
            navController = navController)
    }
}

@Composable
fun PostLists(
    posts:List<PostData>,
    modifier: Modifier,
    loading:Boolean,
    navController: NavController,
    vm:IGViewModel,
    currentUserId:String
){
    Box(modifier = modifier) {
        LazyColumn{
            items(items = posts){
                Post(
                    post = it,
                    currentUserId = currentUserId,
                    vm = vm){
                        navigateTo(navController,DestinationScreen.SinglePost,NavParams("posts",it))
                }
            }
        }
        if(loading){
            ShimmerAnimation()
        }
    }
}

@Composable
fun Post(
    post: PostData,
    currentUserId: String,
    vm: IGViewModel,
    onPostClick: () -> Unit){

    val likeAnimation  = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false)}
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                top = MaterialTheme.spacing.extraSmall,
                bottom = MaterialTheme.spacing.extraSmall
            )
    ) {
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
                verticalAlignment = Alignment.CenterVertically) {
                    Card(shape = CircleShape, modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)) {
                        CommonImage(data = post.userImage, contentScale = ContentScale.Crop)
                    }
                Text(text = post.username ?: "" , modifier = Modifier.padding(4.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .defaultMinSize(minHeight = 150.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {

                                if (post.likes?.contains(currentUserId) == true) {
                                    dislikeAnimation.value = true
                                } else {
                                    likeAnimation.value = true
                                }
                                vm.onLikePost(post)
                            },
                            onTap = {
                                onPostClick.invoke()
                            }
                        )
                    }

                CommonImage(
                    data = post.postImage,
                    modifier = modifier ,
                    contentScale = ContentScale.FillBounds)

                if(likeAnimation.value){
                    LaunchedEffect(key1 = likeAnimation.value){
                        delay(1000)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }

                if(dislikeAnimation.value){
                    LaunchedEffect(key1 = dislikeAnimation.value){
                        delay(1000)
                        dislikeAnimation.value = false
                    }
                    LikeAnimation(false)
                }

            }
            Text(text = post.username ?: "", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

        }
    }
}
