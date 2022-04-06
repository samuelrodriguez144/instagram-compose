package com.example.instagramclone


import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject


const val USERS = "users"
const val POST = "posts"
@HiltViewModel
class IGViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
): ViewModel() {

    var isSignedIn = mutableStateOf(false)
    var inProgress = mutableStateOf(false)
    var userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val  refreshPostProgress  = mutableStateOf(false)
    val post = mutableStateOf<List<PostData>>(listOf())

    val searchedPost = mutableStateOf<List<PostData>>(listOf())
    val searchedPostProgress = mutableStateOf(false)

    val postFeed = mutableStateOf<List<PostData>>(listOf())
    val postFeedProgress = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        isSignedIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun onSignup(username:String,email:String,password:String){
        if(username.isEmpty() or email.isEmpty() or password.isEmpty()){
            handleExecption(null,"Please input all the fields")
            return
        }
        inProgress.value = true

        db.collection(USERS).whereEqualTo("username",username).get()
            .addOnSuccessListener { documents ->
                if(documents.size() > 0 ){
                    handleExecption(null,"Username already taken.")
                    inProgress.value = false
                }
                else{
                    auth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                isSignedIn.value = true
                                createOrUpdateProfile(username = username,)

                            }
                            else{
                                handleExecption(task.exception,"Signed Up Failed")
                            }
                            inProgress.value = false
                        }
                }

            }
            .addOnFailureListener {
                handleExecption(it,"Signed Up Failed")
                inProgress.value = false
            }

    }

    fun onLogin(username: String,password: String){
        if(username.isEmpty() or password.isEmpty()){
            handleExecption(null,"Please input all the fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(username,password)
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    isSignedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                }
                else{
                    handleExecption(task.exception,"Login Failed")
                    inProgress.value = false
                }

            }
            .addOnFailureListener {
                handleExecption(it,"Login Failed")
                inProgress.value = false

            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ){
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let { it ->
            inProgress.value = true
            db.collection(USERS).document(it).get()
                .addOnSuccessListener { task ->
                    if(task.exists()){
                        task.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener { error ->
                                handleExecption(error,"Cannot Update User")
                                inProgress.value = false
                            }
                    }
                    else{
                        db.collection(USERS).document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { error ->
                    handleExecption(error,"Cannot create user")
                    inProgress.value = false

                }
        }
    }

    private fun getUserData(uid:String){
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPost()
                getPersonalizedFeed()
            }
            .addOnFailureListener {
                handleExecption(it,"Cannot retrieve user data")
                inProgress.value = false
            }
    }

    fun handleExecption(exception: Exception? = null,customMessage: String = ""){
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if(customMessage.isEmpty()) errorMessage else "$customMessage: $errorMessage"
        popupNotification.value = Event(message)
    }

    fun updateProfileData(name:String,username:String,bio:String){
        createOrUpdateProfile(name, username,bio)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit){
        inProgress.value = true
        val storageRef =  storage.reference
        val uuid= UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener (onSuccess)
            }
            .addOnFailureListener{
                handleExecption(it,"Cannot upload image")
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri:Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
            updatePostUserImageData(it.toString())
        }
    }

    private fun updatePostUserImageData(imageUrl: String){
        val currentUid = auth.currentUser?.uid
        db.collection(POST).whereEqualTo("userId",currentUid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it,posts)
                val refs = arrayListOf<DocumentReference>()
                for(post in posts.value){
                    post.postId?.let { id->
                        refs.add(db.collection(POST).document(id))
                    }
                }
                if(refs.isNotEmpty()){
                    db.runBatch{ batch ->
                        for(ref in refs){
                            batch.update(ref,"userImage",imageUrl)
                        }
                    }
                        .addOnSuccessListener {
                            refreshPost()
                        }
                }

            }
    }

    fun onLogout(){
        auth.signOut()
        isSignedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged Out.")
        searchedPost.value = listOf()
        postFeed.value = listOf()

    }

    fun onNewPost(uri:Uri, description:String, onPostSuccess:()->Unit){
        uploadImage(uri){
            onCreatePost(it, description = description, onPostSuccess = onPostSuccess )
        }
    }

    private fun onCreatePost(imageUri:Uri,description: String,onPostSuccess: () -> Unit){
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        val fillerWords = listOf("is","the","be","a","in","it","or","and")
        val searchTerms = description
            .split(" ",".","?","!","#")
            .map { it.lowercase() }
            .filter { it.isNotEmpty() and !fillerWords.contains(it) }

        if(currentUid != null){
            val postUid = UUID.randomUUID().toString()
            val post = PostData(
                postId = postUid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )
            db.collection(POST).document(postUid).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event("Post Successfully created")
                    inProgress.value = false
                    onPostSuccess.invoke()
                    refreshPost()
                }
                .addOnFailureListener { exec ->
                    handleExecption(exec,"Unable to create Post")
                    inProgress.value = false
                }
        }
        else{
            handleExecption(customMessage = "Error: Username unavailable. unable to create post")
            onLogout()
            inProgress.value = false
        }
    }

    private fun refreshPost(){
        val currentUid = auth.currentUser?.uid
        if(currentUid != null){
            refreshPostProgress.value = true
            db.collection(POST).whereEqualTo("userId",currentUid).get()
                .addOnSuccessListener { document ->
                    convertPosts(document,post)
                    refreshPostProgress.value = false

                }
                .addOnFailureListener { exec ->
                    handleExecption(exec,"Cannot fetch posts")
                    refreshPostProgress.value = false
                }
                }
        else{
            handleExecption(customMessage = "Error: username unavailable please login. Unable to refresh post")
            onLogout()
        }
    }

    private fun convertPosts(documents:QuerySnapshot,outState: MutableState<List<PostData>>){
        val newPost = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPost.add(post)
        }
        val sortedPost = newPost.sortedByDescending { it.time }
        outState.value = sortedPost
    }

    fun searchPost(searchedTerm:String){
        if(searchedTerm.isNotEmpty()){
            searchedPostProgress.value = true
            db
                .collection(POST)
                .whereArrayContains("searchTerms",searchedTerm.trim().lowercase())
                .get().addOnSuccessListener {
                    convertPosts(it,searchedPost)
                    searchedPostProgress.value = false
                }
                .addOnFailureListener { exec ->
                    handleExecption(exec,"Cannot Search Post")
                    searchedPostProgress.value = false
                }
        }
    }

    fun onFollowClick(userId:String){
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if(following.contains(userId)){
                following.remove(userId)
            }
            else{
                following.add(userId)
            }
            db
                .collection(USERS)
                .document(currentUser)
                .update("following",following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed(){
        val following = userData.value?.following
        postFeedProgress.value = true
        if(!following.isNullOrEmpty()){
            db.collection(POST).whereIn("userId",following).get()
                .addOnSuccessListener {

                    convertPosts(documents = it, outState = postFeed)
                    if(postFeed.value.isEmpty()){
                        getGeneralFeed()
                    }
                    else{
                        postFeedProgress.value = false
                    }
                }
                .addOnFailureListener {
                    handleExecption(it,"Can't retrieve feed")
                    postFeedProgress.value = false
                }
        }
        else{
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed(){
        postFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000
        db.collection(POST).whereGreaterThan("time",currentTime - difference)
            .get()
            .addOnSuccessListener {
                convertPosts(documents = it, outState = postFeed)
                postFeedProgress.value = false
            }
            .addOnFailureListener {
                handleExecption(it,"can't retrieve feed")
                postFeedProgress.value = false
            }
    }

    fun onLikePost(postData:PostData){
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if(likes.contains(userId)){
                    newLikes.addAll(likes.filter { userId != it })
                }
                else{
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    db.collection(POST).document(postId).update("likes",newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleExecption(it,"Unable to like post")
                        }
                }
            }
        }
    }
}