package com.appliances.recycle

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appliances.recycle.adapter.NoticeAdapter
import com.appliances.recycle.dto.Notice
import com.appliances.recycle.retrofit.INetworkService
import com.appliances.recycle.retrofit.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeListFragment : Fragment() {

    private lateinit var noticeAdapter: NoticeAdapter
    private lateinit var recyclerView: RecyclerView
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 10
    private lateinit var networkService: INetworkService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트의 레이아웃을 inflate 합니다.
        return inflater.inflate(R.layout.fragment_notice_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val myApplication = requireActivity().applicationContext as MyApplication
        networkService = myApplication.networkService  // 인증이 필요 없는 API 사용

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        noticeAdapter = NoticeAdapter(listOf()) { notice ->
            val intent = Intent(requireContext(), NoticeDetailActivity::class.java)
            intent.putExtra("nno", notice.nno)
            startActivity(intent)
        }

        recyclerView.adapter = noticeAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItem + 2)) {
                    loadNotices(currentPage, pageSize)
                }
            }
        })

        // 데이터 로드 시작
        loadNotices(currentPage, pageSize)
    }

    private fun loadNotices(page: Int, size: Int) {
        isLoading = true

        networkService.getNotices(page = page, size = size).enqueue(object : Callback<List<Notice>> {
            override fun onResponse(call: Call<List<Notice>>, response: Response<List<Notice>>) {
                if (response.isSuccessful) {
                    response.body()?.let { notices ->
                        val updatedList = noticeAdapter.getNotices() + notices
                        noticeAdapter.updateNotices(updatedList)
                        currentPage++
                    }
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<Notice>>, t: Throwable) {
                isLoading = false
            }
        })
    }

}