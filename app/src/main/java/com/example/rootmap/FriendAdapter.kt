package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.rootmap.databinding.DialogLayooutBinding


class FriendAdapter(): RecyclerView.Adapter<FriendHolder>() {
    var list = mutableListOf<Friend>()
    var myid: String = "id"
    var mode: String = "De"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val binding =
            FriendLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendHolder(binding, mode, myid, parent)
    }
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        Log.d("add_screen_check", "onBind")
        var screen = list.get(position)
        holder.setFriend(screen)
    }
}
class FriendHolder(
    val binding: FriendLayoutBinding,
    mode: String,
    myid: String,
    parent: ViewGroup,
) :
    RecyclerView.ViewHolder(binding.root) {
    var db = Firebase.firestore
    var mode = mode
    var myid = myid
    var parent = parent
    init {

    }
    fun setFriend(friend: Friend) {
        //해당 binding화면에 친구 정보 제공
        binding.friendName.text = friend.nickname
        binding.friendId.text = friend.id
        var frid = friend.id
        when (mode) {
            "List" -> {
                binding.friendButton2.setOnClickListener {// 버튼 기능
                    showDialog(frid, "act")
                }
            }

            "Request" -> {
                binding.friendButton.text = "거절"
                binding.friendButton2.text = "수락"
                binding.friendButton.visibility = View.VISIBLE
                binding.friendButton.setOnClickListener { //거절 버튼 기능
                    showDialog(frid, "cancle")
                }
                binding.friendButton2.setOnClickListener {//수락 버튼 기능
                    showDialog(frid, "accept")
                }
            }

            "Add" -> {
                binding.friendButton2.text = "취소"
                binding.friendButton2.setOnClickListener {//취소 버튼 기능
                    showDialog(frid, "cancle")
                    parent.isInvisible
                }
            }
        }
        //친구에게 프로필 사진 정보가 있으면
        //binding.picture.setImageResource(R.drawable.ic_launcher_foreground)
    }

    fun showDialog(frid: String, d_mode: String) {
        val dBinding =
            DialogLayooutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val dialogBuild = AlertDialog.Builder(binding.root.context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        when (d_mode) {
            "accept" -> {
                dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
                dBinding.bButton.text = "확인"
                dBinding.content.text = "수락하시겠습니까?"
                dBinding.bButton.setOnClickListener {
                    //검정 버튼의 기능 구현 ↓
                    db.collection("user").document(myid).collection("friend").document(frid)
                        .update("state", "2").addOnSuccessListener {
                            Log.d("button_test", "변경")
                        }.addOnFailureListener {
                            Log.d("button_test", "실패")
                        }//내 데이터 변경
                    db.collection("user").document(frid).collection("friend").document(myid)
                        .update("state", "2") //친구 데이터 변경
                    dialog.dismiss()
                }

            }
            "cancle" -> {
                dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
                dBinding.bButton.text = "확인"
                dBinding.content.text = "삭제하시겠습니까?"
                dBinding.bButton.setOnClickListener {
                    //검정 버튼의 기능 구현 ↓
                    db.collection("user").document(myid).collection("friend").document(frid)
                        .delete()
                    db.collection("user").document(frid).collection("friend").document(myid)
                        .delete()
                    dialog.dismiss()
                }

            }
            "act" -> {
                dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
                dBinding.bButton.text = "확인"
                dBinding.content.text = "여긴 아직 아무 기능 없음"
                dBinding.bButton.setOnClickListener {
                    //검정 버튼의 기능 구현 ↓
                    dialog.dismiss()
                }
            }
        }
        dBinding.wButton.setOnClickListener {
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }
    fun re(){}

}
