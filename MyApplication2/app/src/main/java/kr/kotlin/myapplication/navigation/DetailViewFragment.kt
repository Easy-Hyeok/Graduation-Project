package kr.kotlin.myapplication.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kr.kotlin.myapplication.R
import kr.kotlin.myapplication.databinding.ActivityCommentBinding
import kr.kotlin.myapplication.databinding.ActivityLoginBinding
import kr.kotlin.myapplication.databinding.FragmentDetailBinding
import kr.kotlin.myapplication.navigation.model.AlarmDTO
import kr.kotlin.myapplication.navigation.model.ContentDTO

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    private lateinit var binding: FragmentDetailBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        // 초기화
        binding = FragmentDetailBinding.inflate(layoutInflater)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerView).adapter = DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerView).layoutManager = LinearLayoutManager(activity)
        Log.d("TAG!@#", "hiㅐonCreateView")
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{
            firestore?.collection("images")?.orderBy("timestamp",Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                // 새로고침
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //Id
            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textView).text = contentDTOs!![position].userId

            //Image (Glide)
            Glide
                .with(holder.itemView.context)
                .load(contentDTOs!![position].imageUrl)
                .into(viewholder.findViewById<ImageView>(R.id.detailviewitem_content_imageView))

            //Explain
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textView).text = contentDTOs!![position].explain

            //Likes
            viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textView).text = "Likes "+ contentDTOs!![position].favoriteCount

            //ProfileImage
            firestore?.collection("profileImages")
                ?.document(contentDTOs[position].uid!!)
                ?.get()
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        var url = task.result?.get("image")

                        if (url != null) {
                            Glide.with(holder.itemView.context).load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(viewholder.findViewById(R.id.detailviewitem_profile_image))
                        }

                    }
                }

            // 좋아요 버튼에 이벤트 추가
            viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageView).setOnClickListener {
                favoriteEvent(position)
            }

            // 계정 이미지 눌렀을 때 (프로필로 이동)
            viewholder.findViewById<ImageView>(R.id.detailviewitem_profile_image).setOnClickListener {
                profilemove(position)
            }

            // 계정 이름 눌렀을 때 (프로필로 이동)
            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textView).setOnClickListener {
                profilemove(position)
            }

            // 댓글 버튼 눌렀을 때
            viewholder.findViewById<ImageView>(R.id.detailviewitem_comment_imageView).setOnClickListener { view ->
                var intent = Intent(view.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                intent.putExtra("destinationUid",contentDTOs[position].uid)
                startActivity(intent)
            }

            if(contentDTOs!![position].favorites.containsKey(uid)){
                 // 좋아요 버튼이 눌려 있을 때
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageView).setImageResource(R.drawable.ic_favorite)
            }
            else{
                 // 좋아요 버튼이 눌려 있지 않을 때
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageView).setImageResource(R.drawable.ic_favorite_border)
            }
        }

        fun profilemove(position: Int){
            var fragment = UserFragment()
            var bundle = Bundle()
            bundle.putString("destinationUid",contentDTOs[position].uid)
            bundle.putString("userId",contentDTOs[position].userId)
            fragment.arguments = bundle
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction{ transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    // 좋아요 버튼이 눌려 있을 때 클릭 (취소)
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites?.remove(uid!!)

                }else{
                    // 좋아요 버튼이 눌려 있지 않을 때 클릭 (좋아요)
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites?.set(uid!!, true)
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc,contentDTO)
            }
        }

        // 좋아요 알림
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            // 좋아요 푸시 이벤트
            var message = FirebaseAuth.getInstance()?.currentUser?.email + " "+ getString(R.string.alarm_favorite)
//            FcmPush.instance.sendMessage(destinationUid,"Stagram",message)
        }
    }
}