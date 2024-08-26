package com.example.rootmap

import android.graphics.Canvas
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FriendLayoutBinding
import kotlin.math.min
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore


//
class MyDocumentAdapter() : RecyclerView.Adapter<MyDocumentAdapter.Holder>()  {
    var list = mutableListOf<MyRouteDocument>()
    lateinit var mode:String
    lateinit var userId:String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            FriendLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding,mode)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list.get(position)
        holder.setData(screen)
    }
    override fun getItemCount(): Int {
        return list.size
    }
    inner class Holder(
        val binding: FriendLayoutBinding,
        val mode: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(myRouteDocument: MyRouteDocument) {
            binding.apply {
                friendName.text = myRouteDocument.docName
                picture.visibility = View.GONE

                // 현재 로그인한 사용자의 이메일 가져오기
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

                if (currentUserEmail != null) {
                    val db = FirebaseFirestore.getInstance()
                    val collectionRef = db.collection("user")
                        .document(currentUserEmail)  // 현재 로그인한 사용자의 이메일로 설정
                        .collection("route")

                    collectionRef.get()
                        .addOnSuccessListener { result ->
                            // 현재 문서의 'shared' 값을 저장할 변수
                            var currentSharedText = ""

                            for (document in result) {
                                val sharedField = document.get("shared")
                                //Log.d("Shared", "$sharedField")
                                val sharedText: String = when (sharedField) {
                                    is List<*> -> {
                                        // List의 각 항목이 String인지 확인하고 문자열로 변환
                                        sharedField.filterIsInstance<String>().joinToString(",\n")
                                    }
                                    else -> "No shared data" // 예상하지 않은 타입일 경우
                                }
                                // 현재 문서와 일치하는 경우, 해당 sharedText를 저장
                                if (document.id == myRouteDocument.docId) {
                                    currentSharedText = sharedText
                                    break // 현재 문서의 `shared` 값만을 찾았으므로 반복 종료
                                }
                            }
                            // 'shared' 필드 값이 포함된 문자열을 binding.friendId에 설정
                            if (myRouteDocument.owner != userId) {
                                val ownerText = myRouteDocument.owner ?: "Unknown owner"
                                val sharedText = "\n님에게 공유받은 경로"
                                val fullText = "$ownerText$sharedText"

                                val spannableString = SpannableString(fullText)
                                val colorBlue = ContextCompat.getColor(binding.root.context, R.color.custum_color)

                                spannableString.setSpan(
                                    ForegroundColorSpan(colorBlue),
                                    0,
                                    ownerText.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                binding.friendId.text = spannableString
                            }
                            else {
                                // 현재 문서의 'shared' 값과 "님과 공유 중"을 결합하여 설정
                                //val sharingText = "\n님과 공유 중"
                                //val fullText = "$currentSharedText"
                                if (currentSharedText != "") {
                                    val sharingText = "\n님과 공유 중"
                                    val fullText = "$currentSharedText$sharingText"
                                    binding.friendId.text = fullText
                                } else binding.friendId.text = ""
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("문서 가져오기 실패: $exception")
                        }
                } else {
                    println("로그인된 사용자가 없습니다.")
                }

                if(mode=="View"){
                    //menu3의 루트 보기
                    binding.friendButton2.text="보기"
                    binding.friendButton2.setOnClickListener {
                        itemClickListener.onListClick(it, position)
                    }
                }else if(mode=="Add"){
                    //menu3에서 검색 후 해당 장소를 추가할 루트 선택 시
                    binding.friendButton2.text="추가"
                    binding.friendButton2.setOnClickListener {
                        itemClickListener.onClick(it, position)
                    }
                }else if(mode=="makePost"){
                    //menu2에서 사용
                    binding.friendButton2.text="선택"
                    binding.friendButton2.setOnClickListener {
                        itemClickListener.onClick(it, position)
                    }
                } else{ //내 여행경로 액티비티의 설정(mode==MyRoute)
                    binding.run{
                        friendButton2.visibility=View.GONE
                        optionButton.visibility=View.VISIBLE
                        optionButton.setOnClickListener {
                            itemClickListener.onClick(it, position)
                        }
                    }
                }
                binding.tvRemove.setOnClickListener {
                    itemClickListener.deleteDoc(it, position)
                }
                binding.tvShare.setOnClickListener {
                    itemClickListener.shareDoc(it, position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onListClick(v: View, position: Int)
        fun deleteDoc(v: View, position: Int)
        fun shareDoc(v: View, position: Int) //수정 버튼 클릭 이벤트
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
}

class SwapeManageAdapter(private var recyclerViewAdapter2 : MyDocumentAdapter) : ItemTouchHelper.Callback() {
    private var currentPosition: Int? = null    // 현재 선택된 recycler view의 position
    private var previousPosition: Int? = null   // 이전에 선택했던 recycler view의 position
    private var currentDx = 0f                  // 현재 x 값
    private var clamp = 0f                      // 고정시킬 크기
    private var check=false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            0,
            LEFT or RIGHT
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f                                      // 현재 x 위치 초기화
        previousPosition = viewHolder.adapterPosition       // 드래그 또는 스와이프 동작이 끝난 view의 position 기억하기
        getDefaultUIUtil().clearView(getView(viewHolder))
    }

    // ItemTouchHelper가 ViewHolder를 스와이프 되었거나 드래그 되었을 때 호출
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            currentPosition = viewHolder.adapterPosition    // 현재 드래그 또는 스와이프 중인 view 의 position 기억하기
            getDefaultUIUtil().onSelected(getView(it))
        }
    }

    // 아이템을 터치하거나 스와이프하는 등 뷰에 변화가 생길 경우 호출
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)      // 고정할지 말지 결정, true : 고정함 false : 고정 안 함
            val newX = clampViewPositionHorizontal(dX, isClamped, isCurrentlyActive)  // newX 만큼 이동(고정 시 이동 위치/고정 해제 시 이동 위치 결정)
            // 고정시킬 시 애니메이션 추가
            if (newX == -clamp) {
                view.animate().translationX(-clamp).setDuration(100L).start()
                return
            }
            currentDx = newX
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                view,
                newX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    // 사용자가 view를 swipe 했다고 간주할 최소 속도 정하기
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10

    // 사용자가 view를 swipe 했다고 간주하기 위해 이동해야하는 부분 반환
    // (사용자가 손을 떼면 호출됨)
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        // -clamp 이상 swipe시 isClamped를 true로 변경 아닐시 false로 변경
        setTag(viewHolder, currentDx <= -clamp)
        return 2f
    }

    // swipe_view 반환 -> swipe_view만 이동할 수 있게 해줌
    private fun getView(viewHolder: RecyclerView.ViewHolder) : View = viewHolder.itemView.findViewById(R.id.swipe_view)

    // swipe_view 를 swipe 했을 때 <삭제> 화면이 보이도록 고정
    private fun clampViewPositionHorizontal(
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ) : Float {
        // RIGHT 방향으로 swipe 막기
        val max = 0f

        // 고정할 수 있으면
        val newX = if (isClamped) {
            // 현재 swipe 중이면 swipe되는 영역 제한
            if (isCurrentlyActive){
                // 오른쪽 swipe일 때
                if (dX < 0){
                    dX/3 - clamp
                }else dX - clamp // 왼쪽 swipe일 때
            }else -clamp// swipe 중이 아니면 고정시키기
        }
        // 고정할 수 없으면 newX는 스와이프한 만큼
        else dX / 2

        // newX가 0보다 작은지 확인
        return min(newX, max)
    }

    // isClamped를 view의 tag로 관리
    // isClamped = true : 고정, false : 고정 해제
    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) { viewHolder.itemView.tag = isClamped }
    private fun getTag(viewHolder: RecyclerView.ViewHolder) : Boolean =  viewHolder.itemView.tag as? Boolean ?: false


    // view가 swipe 되었을 때 고정될 크기 설정
    fun setClamp(clamp: Float) { this.clamp = clamp }

    // 다른 View가 swipe 되거나 터치되면 고정 해제
    fun removePreviousClamp(recyclerView: RecyclerView) {
        // 현재 선택한 view가 이전에 선택한 view와 같으면 패스
        if (currentPosition == previousPosition) return
        // 이전에 선택한 위치의 view 고정 해제
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).animate().x(0f).setDuration(100L).start()
            setTag(viewHolder, false)
            previousPosition = null
        }
    }

    fun removeClamp(recyclerView: RecyclerView){
        currentPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).animate().x(0f).setDuration(100L).start()
            setTag(viewHolder, false)
            currentPosition = null
        }
    }


}