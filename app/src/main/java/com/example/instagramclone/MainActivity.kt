package com.example.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.auth.LoginScreen
import com.example.instagramclone.auth.ProfileScreen
import com.example.instagramclone.auth.SignupScreen
import com.example.instagramclone.data.PostData
import com.example.instagramclone.main.*
import com.example.instagramclone.ui.theme.InstagramCloneTheme
import com.example.instagramclone.ui.theme.spacing
import com.example.instagramclone.utils.AppTheme
import com.example.instagramclone.utils.ThemeSetting
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeSetting: ThemeSetting


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = themeSetting.themeStream.collectAsState()
            val useDarkColors = when(theme.value){
                AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                AppTheme.MODE_DAY -> false
                AppTheme.MODE_NIGHT -> true
            }
            InstagramCloneTheme(darkTheme = useDarkColors) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route:String){
    object Signup: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Feed: DestinationScreen("feed")
    object Search: DestinationScreen("search")
    object MyPosts: DestinationScreen("mypost")
    object MyProfile: DestinationScreen("profile")
    object NewPost: DestinationScreen("newpost/{imageUri}"){
        fun createRoute(uri:String) = "newpost/$uri"
    }
    object SinglePost: DestinationScreen("singlepost")
    object CommentsScreen: DestinationScreen("comments/{postId}"){
        fun createRoute(postId:String) = "comments/$postId"
    }

}

@Composable
fun InstagramApp(){
    val vm = hiltViewModel<IGViewModel>()
    val navController = rememberNavController()
    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route){
        composable(DestinationScreen.Signup.route){
            SignupScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController = navController, vm = vm )
        }
        composable(DestinationScreen.Feed.route){
            FeedScreen(navController = navController, vm = vm )
        }
        composable(DestinationScreen.Search.route){
            SearchScreen(navController = navController, vm = vm )
        }
        composable(DestinationScreen.MyPosts.route){
            MyPostScreen(navController = navController, vm = vm )
        }
        composable(DestinationScreen.MyProfile.route){
            ProfileScreen(navController = navController, vm = vm )
        }
        composable(DestinationScreen.NewPost.route){ navBackStackEntry ->
            var imageUri = navBackStackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, vm = vm ,it)
            }

        }
        composable(DestinationScreen.SinglePost.route){
            val postData = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<PostData>("posts")
            postData?.let {
                SinglePostScreen(navController = navController, vm = vm, post = postData)
            }
        }
        composable(DestinationScreen.CommentsScreen.route){ navBackStackEntry ->
            val postId = navBackStackEntry.arguments?.getString("postId")
            postId?.let {
                CommentsScreen(navController = navController, vm = vm, postId = it )
            }
        }
    }
}




