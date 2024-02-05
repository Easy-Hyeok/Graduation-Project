package kr.kotlin.myapplication.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.fido.fido2.api.common.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kr.kotlin.myapplication.R
import kr.kotlin.myapplication.databinding.ActivityCommentBinding
import kr.kotlin.myapplication.databinding.ActivityLoginBinding
import kr.kotlin.myapplication.databinding.FragmentAlarmBinding
import kr.kotlin.myapplication.navigation.model.AlarmDTO
import kr.kotlin.myapplication.navigation.model.ContentDTO

class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    var destinationUid : String? = null
    private lateinit var binding: ActivityCommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        binding = ActivityCommentBinding.inflate(layoutInflater)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        binding.commentRecyclerView .adapter = CommentRecyclerviewAdapter()
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)

        // 댓글 입력
       binding.commentButton?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditView.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            commentAlarm(destinationUid!!,binding.commentEditView.text.toString())
           binding.commentEditView.setText("")
        }
    }

    // 댓글 알림
    fun commentAlarm(destinationUid : String, message : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 댓글 푸시 이벤트
        var msg = FirebaseAuth.getInstance()?.currentUser?.email + " "+ getString(R.string.alarm_comment)+ " of " + message
//        FcmPush.instance.sendMessage(destinationUid,"Stagram",msg)
    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init{
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot == null)return@addSnapshotListener

                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view:View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//            var view = holder.itemView
//            view.commentview_comment_textView.text = comments[position].comment
//            view.commentview_profile_textView.text = comments[position].userId
//
//            FirebaseFirestore.getInstance()
//                ?.collection("profileImages")
//                ?.document(comments[position].uid!!)
//                ?.get()
//                ?.addOnCompleteListener { task ->
//                    if(task.isSuccessful){
//                        var url = task.result?.get("image")
//                        if (url != null) {
//                            Glide.with(holder.itemView.context).load(url)
//                                .apply(RequestOptions().circleCrop())
//                                .into(view.commentview_profile_imageView)
//                        }
//                    }
//            }
        }
    }
}