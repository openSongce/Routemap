package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import android.app.Activity
import com.example.rootmap.databinding.FragmentFriendAddBinding

class FriendAdapter constructor(mode:String,id:String): RecyclerView.Adapter<FriendHolder>(){
    var list= mutableListOf<Friend>()
    var mode=mode
    init {
        list.clear()
        var db=Firebase.firestore
        var myDb=db.collection("user").document(id).collection("friend")
        when(mode){
            "Add"->{
                myDb.whereEqualTo("state","1").addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    for (snapshot in querySnapshot!!.documents) {
                        val id=snapshot.data?.get("id").toString()
                        db.collection("user").document(id).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                            val name= querySnapshot?.data?.get("name").toString()
                            var load=Friend(name,id)
                            list.add(load)
                        }
                    }
                    if (list.isNullOrEmpty()){
                       // binding.friendAddText.text="수락 대기 중인 친구 없음"
                        //binding.friendAddText.visibility= View.VISIBLE
                    }
                    notifyDataSetChanged()
                }
            }
            "List"->{
                myDb.whereEqualTo("state","2").addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    for (snapshot in querySnapshot!!.documents) {
                        val id=snapshot.data?.get("id").toString()
                        db.collection("user").document(id).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                            val name= querySnapshot?.data?.get("name").toString()
                            var load=Friend(name,id)
                            list.add(load)
                        }
                    }
                    if (list.isNullOrEmpty()){
                        // binding.friendAddText.text="친구 없음"
                        //binding.friendAddText.visibility= View.VISIBLE
                    }
                    notifyDataSetChanged()
                }
            }
            "Request"->{
                myDb.whereEqualTo("state","0").addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    for (snapshot in querySnapshot!!.documents) {
                        val id=snapshot.data?.get("id").toString()
                        db.collection("user").document(id).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                            val name= querySnapshot?.data?.get("name").toString()
                            var load=Friend(name,id)
                            list.add(load)
                        }
                    }
                    if (list.isNullOrEmpty()){
                        // binding.friendAddText.text="받은 요청 없음"
                        //binding.friendAddText.visibility= View.VISIBLE
                    }
                    notifyDataSetChanged()
                }

            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val binding=FriendLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        when(mode){
            "Add"->{
                binding.friendButton.text="취소"
            }
        }
        return FriendHolder(binding)
    }
    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val screen=list.get(position)
        holder.setFriend(screen)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}

class FriendHolder(val binding: FriendLayoutBinding): RecyclerView.ViewHolder(binding.root){
    init {
        /*클릭 시 발생할 이벤트 코드*/

    }
    fun setFriend(friend: Friend){
        //해당 binding화면에 친구 정보 제공
        binding.friendName.text=friend.name
        binding.friendId.text=friend.id
        //친구에게 프로필 사진 정보가 있으면
        //binding.picture.setImageResource(R.drawable.ic_launcher_foreground)

    }

}