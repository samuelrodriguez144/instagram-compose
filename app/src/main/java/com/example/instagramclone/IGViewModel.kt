package com.example.instagramclone


import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject
import com.example.instagramclone.data.UserData
import com.google.firebase.firestore.ktx.toObject
import java.util.*


const val USERS = "users"
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

    init {
//        auth.signOut()
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
        }
    }

    fun onLogout(){
        auth.signOut()
        isSignedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged Out.")
    }

}