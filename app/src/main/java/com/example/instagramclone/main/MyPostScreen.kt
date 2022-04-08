package com.example.instagramclone.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IGViewModel
import com.example.instagramclone.R
import com.example.instagramclone.data.PostData
import com.example.instagramclone.ui.theme.PinkTheme
import com.example.instagramclone.ui.theme.spacing
import com.example.instagramclone.ui.theme.animation.ShimmerAnimation

data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null
){
    fun isFull() = post1 != null && post2 != null && post3 != null
    fun add(post: PostData){
        when {
            post1 == null -> {
                post1 = post
            }
            post2 == null -> {
                post2 = post
            }
            post3 == null -> {
                post3 = post
            }
        }
    }

}

@Composable
fun MyPostScreen(navController: NavController,vm:IGViewModel){

    PinkTheme(darkTheme = false){
        MyPostScreenTopBar(navController,vm)
    }

}

@Composable
fun MyPostScreenTopBar(navController: NavController,vm:IGViewModel){

    val userData = vm.userData.value
    val menuExpanded = remember {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =Arrangement.Start){
                        Text(text = userData?.username ?: "")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            menuExpanded.value = true
                        }) {
                        Icon(Icons.Filled.Settings, contentDescription = null )
                    }
                    Column(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    }
                }
            )
        },
        content = {
            MyPostScreenContent(navController = navController,vm=vm)
        }
    )

}

@Composable
fun MyPostScreenContent(navController: NavController,vm:IGViewModel){
    val newPostImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){ uri ->
        uri?.let {
            val encodedImage = Uri.encode(it.toString())
            val route = DestinationScreen.NewPost.createRoute(encodedImage)
            navController.navigate(route)
        }

    }
    val userData = vm.userData.value
    val isLoading = vm.inProgress.value
    val postLoading = vm.refreshPostProgress.value
    val posts = vm.post.value
    val followers = vm.followers.value

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row() {
                ProfileImage(userData?.imageUrl){
                    newPostImageLauncher.launch("image/*")
                }
                Text(text = "${posts.size}\nPosts",
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = CenterVertically),
                    textAlign = TextAlign.Center)
                Text(text = "${followers}\nFollowers",
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = CenterVertically),
                    textAlign = TextAlign.Center)
                Text(text = "${userData?.following?.size ?: 0}\nFollowing",
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
                    .padding(MaterialTheme.spacing.small)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(10)) {
                Text(text = "Edit Profile")
            }
            Column(modifier = Modifier.weight(1f)) {
                PostList(
                    isContextLoading = isLoading,
                    postLoading = postLoading,
                    posts = posts ,
                    modifier = Modifier
                        .weight(1f)
                        .padding(1.dp)
                        .fillMaxSize()
                ){ post ->
                    navigateTo(
                        navController = navController,
                        DestinationScreen.SinglePost,
                        NavParams("posts",post))
                }
            }

        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationMenuItem.POST,
            navController = navController)

        if(isLoading){
            ShimmerAnimation()
        }
    }
}


@Composable
fun ProfileImage(imageUrl:String?, onclick: ()->Unit){

    Box(modifier = Modifier
        .padding(top = MaterialTheme.spacing.small)
        .clickable { onclick.invoke() }) {

        UserImageCard(
            userImage = imageUrl,
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .size(80.dp))

        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(26.dp)
                .align(BottomEnd)
                .offset(x = (-12).dp, y = (-12).dp)
                ) {

            Image(painter = painterResource(id = R.drawable.ic_plus), contentDescription = null, modifier = Modifier
                .background(Color.Black))
        }
    }

}

@Composable
fun PostList(
    isContextLoading:Boolean,
    postLoading:Boolean,
    posts: List<PostData>,
    modifier:Modifier,
    onPostClick:(PostData) -> Unit){

    when {
        postLoading -> {
            CommonProgressSpinner()
        }
        posts.isEmpty() -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if(!isContextLoading){
                    Text(text = "No Post Available")
                }
            }
        }
        else -> {
            LazyColumn(modifier = modifier) {
                val rows = arrayListOf<PostRow>()
                var currentRow = PostRow()
                rows.add(currentRow)
                for(post in posts){
                    if(currentRow.isFull()){
                        currentRow = PostRow()
                        rows.add(currentRow)

                    }
                    currentRow.add(post = post)
                }

                items(items = rows){ row ->
                    PostsRow(item = row, onPostClick = onPostClick)
                }
            }
        }
    }
}

@Composable
fun PostsRow(item:PostRow,onPostClick: (PostData) -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)){
        PostImage(imageURL = item.post1?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post1?.let { post -> onPostClick(post) } })
        PostImage(imageURL = item.post2?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post2?.let { post -> onPostClick(post) } })
        PostImage(imageURL = item.post3?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post3?.let { post -> onPostClick(post) } })
    }
}

@Composable
fun PostImage(imageURL:String?,modifier: Modifier){
    Box(modifier = modifier){
        var modifier = modifier
            .padding(1.dp)
            .fillMaxSize()
        if(imageURL == null){
            modifier = modifier.clickable(enabled = false) {}
        }
        CommonImage(data = imageURL, modifier = modifier, contentScale = ContentScale.Crop)
    }
}



