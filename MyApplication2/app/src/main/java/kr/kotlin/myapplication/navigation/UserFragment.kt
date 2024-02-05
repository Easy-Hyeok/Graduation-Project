package kr.kotlin.myapplication.navigation

import android.app.Application
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kr.kotlin.myapplication.LoginActivity
import kr.kotlin.myapplication.MainActivity
import kr.kotlin.myapplication.R
import kr.kotlin.myapplication.databinding.ActivityLoginBinding
import kr.kotlin.myapplication.databinding.FragmentGridBinding
import kr.kotlin.myapplication.databinding.FragmentUserBinding
import kr.kotlin.myapplication.navigation.model.AlarmDTO
import kr.kotlin.myapplication.navigation.model.ContentDTO
import kr.kotlin.myapplication.navigation.model.FollowDTO
import org.w3c.dom.Text

class UserFragment : Fragment(){
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    var loadfollow : ListenerRegistration? = null
    var loadprofileimage : ListenerRegistration? = null
    var profileImageView: ImageView? = null
    private lateinit var cbActivityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG="UserFragment"
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    init {
        cbActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                ar-> val uri = ar.data?.data
            Log.d(TAG, "프로필 클릭2")
            profileImageView?.setImageURI(uri)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        // 넘어온 Uid 받아오기
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        fragmentView.apply{
            val accountProfileButton = this?.findViewById<Button>(R.id.account_profile_button)
            if (uid == currentUserUid) {
                // 나의 유저 페이지
                accountProfileButton?.text = getString(R.string.signout)
                accountProfileButton?.setOnClickListener {
                    activity?.finish()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    auth?.signOut()
                }
                // 프로필 사진 클릭
                profileImageView=this?.findViewById<ImageView>(R.id.account_profile_imageView)
                profileImageView
                    ?.setOnClickListener {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    Log.d(TAG, "프로필 클릭1")
                    Log.d(TAG, "프로필 클릭3")
                    cbActivityResultLauncher.launch(photoPickerIntent)
                    Log.d(TAG, "프로필 클릭4")
//                    activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
                }

            } else {
                // 다른 유저 페이지
                accountProfileButton?.text = getString(R.string.follow)
                var mainActivity = (activity as MainActivity)
                mainActivity.apply{
                    this?.findViewById<ImageView>(R.id.toolbar_back_Button)?.setOnClickListener{
                        this.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.home
                    } // 디테일 화면으로 돌아가기

                    this?.findViewById<ImageView>(R.id.top_image)?.visibility = View.GONE
                    this?.findViewById<ImageView>(R.id.toolbar_back_Button)?.visibility = View.VISIBLE
                    this?.findViewById<TextView>(R.id.user_name)?.visibility = View.VISIBLE

                }
//                mainActivity?.findViewById<Toolbar>(R.id.my_toolbar)?.text = arguments?.getString("userId")

                fragmentView?.findViewById<Button>(R.id.account_profile_button)?.setOnClickListener {
                    requestFollow()
                }
            }

//             어댑터 연결
            val accountRecyclerView = fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerView)
            accountRecyclerView?.adapter = UserFragmentRecyclerViewAdapter()
            accountRecyclerView?.layoutManager = GridLayoutManager(requireActivity(),3)
        }

            return fragmentView
        }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "uid:${uid}")

        // 팔로우, 팔로워 수 불러오기
        loadfollow = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            fragmentView.apply{

                if(followDTO?.followerCount != null){
                    this?.findViewById<TextView>(R.id.account_follower_count)?.text = followDTO?.followerCount?.toString()
                }
                if(followDTO?.followingCount != null){
                    this?.findViewById<TextView>(R.id.account_following_count)?.text = followDTO?.followingCount?.toString()
                    val boolean: Boolean? = followDTO?.followers?.containsKey(currentUserUid!!)
                    if(boolean != null && boolean){
                        this?.findViewById<TextView>(R.id.account_profile_button)?.text = getString(R.string.follow_cancel)
                        fragmentView?.findViewById<Button>(R.id.account_profile_button)?.background?.setColorFilter(ContextCompat.getColor(requireActivity(),
                            R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                    }
                    else{
                        if(uid!= currentUserUid){
                            val accountProfileButton : Button? = this?.findViewById(R.id.account_profile_button)
                            accountProfileButton?.text = getString(R.string.follow)
                            accountProfileButton?.background?.colorFilter = null
                        }
                    }
                }
            }
        }

        // 프로필 사진 이미지 불러오기
        loadprofileimage = firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.findViewById(R.id.account_profile_imageView!!))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 스냅샷 제거(오류 방지)
        loadfollow?.remove()
        loadprofileimage?.remove()
    }


    // 팔로워 알림
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 팔로워 푸시 이벤트
        var message = auth?.currentUser?.email + " "+getString(R.string.alarm_follow)
//        FcmPush.instance.sendMessage(destinationUid,"Stagram",message)
    }

    // 팔로우
    fun requestFollow(){
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction{ transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null){
                // 팔로우 정보가 없을 때 생성
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                // 팔로우 하고 있는 경우 (취소)
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followings?.remove(uid)
            }
            else{
                // 팔로우 하고 있지 않은 경우 (팔로우)
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followings?.set(uid!!, true)
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)

                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                // 팔로우 하고 있는 경우
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            }
            else{
                // 팔로우 하고 있지 않은 경우
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    // 데이터 받아오기
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    fragmentView?.findViewById<TextView>(R.id.account_post_count)?.text =
                        contentDTOs.size.toString()
                    notifyDataSetChanged()

                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()
            ).into(imageView)

        }
    }
}