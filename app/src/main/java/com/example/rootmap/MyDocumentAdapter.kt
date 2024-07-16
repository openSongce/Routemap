package com.example.rootmap

import android.graphics.Canvas
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FriendLayoutBinding
import kotlin.math.min


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
        val mode:String
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(myRouteDocument: MyRouteDocument) {
            // binding.friendName.text=myRouteDocument.docName
            binding.apply {
                friendName.text = myRouteDocument.docName
                picture.visibility = View.GONE

                if (myRouteDocument.owner != userId) {
                    val ownerText = myRouteDocument.owner
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
                    friendId.text = spannableString
                } else {
                    friendId.text = ""
                }
            }
            if(mode=="View"){
                binding.friendButton2.text="보기"
                binding.friendButton2.setOnClickListener {
                    itemClickListener.onListClick(it, position)
                }
            }else if(mode=="Add"){
                binding.friendButton2.text="추가"
                binding.friendButton2.setOnClickListener {
                    itemClickListener.onClick(it, position)
                }
            }else if(mode=="makePost"){
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
