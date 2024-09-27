package com.example.rootmap

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.DialogChangePasswordBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentFriendListBinding
import com.example.rootmap.databinding.FriendInfoDialogBinding
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import androidx.fragment.app.DialogFragment

//친구 리스트 프래그먼트-현재 자신과 친구 상태인 유저의 리스트를 출력 등의 기능을 가진 화면
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendList : Fragment(), ConfirmDialogInterface{
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var mode: String? = null

    //프래그먼트의 binding
    val binding by lazy { FragmentFriendListBinding.inflate(layoutInflater) }
    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    val data: MutableList<Friend> = mutableListOf()
    lateinit var listAdapter:FriendAdapter
    lateinit var fr:Fragment
    init{
        instance = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id")
            mode = it.getString("mode")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //binding 지정
        myDb = db.collection("user").document(currentId.toString()).collection("friend")
        listAdapter = FriendAdapter()
        listAdapter.mode="List"
        listAdapter.myid=currentId.toString()
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listAdapter.list = data
        viewLifecycleOwner.lifecycleScope.async {
            refresh()
            binding.recyclerList.adapter = listAdapter
            binding.recyclerList.layoutManager = LinearLayoutManager(context)
        }
        fr=this

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }
    fun refresh(){
        data.clear()
        viewLifecycleOwner.lifecycleScope.async {
            loadData()
         //   listAdapter.list = data
            if (data.isEmpty()) {
                binding.friendListText.text = "친구가 없습니다."
                binding.friendListText.visibility = View.VISIBLE
            }else{
                binding.friendListText.visibility = View.INVISIBLE
            }
            listAdapter?.notifyDataSetChanged()
        }
    }

    fun showAction(frname:String,frid:String){ //친구 클릭 시
        val dBinding = FriendInfoDialogBinding.inflate(layoutInflater)
        dBinding.infoFirendId.text=frid
        dBinding.infoFriendNName.text=frname

        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dBinding.frDelButton.setOnClickListener {
               val deletDialog=ConfirmDialog(this,"해당 유저를 친구에서 삭제하시겠습니까?",frid)
             deletDialog.isCancelable = false
             deletDialog.show(activity?.supportFragmentManager!!, "ConfirmDialog")
        }
    }
    suspend fun loadData(): Boolean {
        return try {
            val fr_add = myDb.whereEqualTo("state", "2").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = db.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("nickname").toString(), id)
                data.add(load)
            }
            Log.d("list_test", "try")
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private var instance: FriendList? = null
        fun getInstance():FriendList?{
            return instance
        }
    }
    override fun onYesButtonClick(id:String) {
        var myid=currentId.toString()
        db.collection("user").document(myid).collection("friend").document(id)
            .delete()
        db.collection("user").document(id).collection("friend").document(myid)
            .delete()
        refresh()
    }


}