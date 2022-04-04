package com.example.instagramclone.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.IGViewModel


@Composable
fun SearchScreen(navController: NavController,vm:IGViewModel){

    val searchPostLoading = vm.searchedPostProgress.value
    val searchedPost = vm.searchedPost.value
    var searchedTerm by rememberSaveable { mutableStateOf("")}

    Column {
        SearchBar(
            searchTerm = searchedTerm,
            onSearchChange = {searchedTerm = it},
            onSearch = {vm.searchPost(searchedTerm)})
        PostList(
            isContextLoading = false,
            postLoading = searchPostLoading,
            posts = searchedPost,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ){ post ->
            navigateTo(navController = navController, dest = DestinationScreen.SinglePost,
                NavParams("posts",post)
            )
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationMenuItem.SEARCH,
            navController = navController)
    }
}

@Composable
fun SearchBar(searchTerm:String,onSearchChange:(String)->Unit, onSearch:()->Unit){
    val focusManager = LocalFocusManager.current
    TextField(
        value = searchTerm,
        onValueChange = onSearchChange,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, CircleShape),
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            textColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = {
                onSearch
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Filled.Search , contentDescription = null)
            }
        }
    )
}