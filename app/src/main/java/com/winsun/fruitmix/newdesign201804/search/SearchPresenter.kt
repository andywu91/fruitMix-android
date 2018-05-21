package com.winsun.fruitmix.newdesign201804.search

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.databinding.ActivitySearchBinding
import com.winsun.fruitmix.file.data.model.*
import com.winsun.fruitmix.model.ViewItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.inflateView
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.presenter.FileRecyclerViewAdapter
import com.winsun.fruitmix.newdesign201804.file.list.presenter.ItemFile
import com.winsun.fruitmix.newdesign201804.file.list.presenter.ItemFolder
import com.winsun.fruitmix.newdesign201804.file.list.presenter.ORIENTATION_LIST_TYPE
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FileItemViewModel
import com.winsun.fruitmix.newdesign201804.file.list.viewmodel.FolderItemViewModel
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.viewmodel.LoadingViewModel
import com.winsun.fruitmix.viewmodel.NoContentViewModel
import com.winsun.fruitmix.viewmodel.ToolbarViewModel

import kotlinx.android.synthetic.main.search_type_item.view.*
import kotlinx.android.synthetic.main.search_type_card.view.*


class SearchPresenter(private val activitySearchBinding: ActivitySearchBinding, val toolbarViewModel: ToolbarViewModel,
                      val loadingViewModel: LoadingViewModel, val noContentViewModel: NoContentViewModel,
                      val fileDataSource: FileDataSource) {

    private val editText = activitySearchBinding.toolbar?.title!!

    private val fileTypeTitleTv = activitySearchBinding.fileTypeTitleTv

    private val selectedSearchTypeItemRecyclerView = activitySearchBinding.selectedSearchTypeItemRecyclerView

    private val searchTypeItemRecyclerView = activitySearchBinding.searchTypeItemRecyclerView

    private val searchedFileRecyclerView = activitySearchBinding.searchedFileRecyclerView

    private val context = editText.context

    private val selectedSearchTypeItems = mutableListOf<SearchTypeItem>()

    private lateinit var searchTypeAdapter: SearchTypeAdapter

    private lateinit var searchTypeCardViewAdapter: SearchTypeCardViewAdpater

    private lateinit var fileRecyclerViewAdapter: FileRecyclerViewAdapter

    private var currentInputText = ""

    fun initView() {

        editText.clearFocus()

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            editText.text.clear()

        }

        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                currentInputText = editText.text.toString()

                handleSelectedSearchTypeItemCountOrInputTextChanged()

            }

        })

        initSearchTypeItem()

        showSearchTypeItemRecyclerView()

    }

    private fun initSearchTypeItem() {

        initFileRecyclerView()

        searchTypeCardViewAdapter = SearchTypeCardViewAdpater {

            selectedSearchTypeItems.remove(it)

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

        selectedSearchTypeItemRecyclerView.layoutManager = LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false)

        selectedSearchTypeItemRecyclerView.adapter = searchTypeCardViewAdapter

        val searchTypeItems = listOf<SearchTypeItem>(

                SearchTypeItem(R.drawable.pdf, "PDFs"),
                SearchTypeItem(R.drawable.word, "Word"),
                SearchTypeItem(R.drawable.excel, "Excel"),
                SearchTypeItem(R.drawable.power_point, "PPT"),
                SearchTypeItem(R.drawable.photo_image, "Photos&Images"),
                SearchTypeItem(R.drawable.video, "Video"),
                SearchTypeItem(R.drawable.audio, "Audio")

        )

        searchTypeAdapter = SearchTypeAdapter {

            selectedSearchTypeItems.add(it)

            handleSelectedSearchTypeItemCountOrInputTextChanged()

        }

        searchTypeAdapter.setItemList(searchTypeItems)

        searchTypeItemRecyclerView.layoutManager = LinearLayoutManager(context)

        searchTypeItemRecyclerView.adapter = searchTypeAdapter

        searchTypeAdapter.notifyDataSetChanged()

    }

    private fun initFileRecyclerView() {

        searchedFileRecyclerView.layoutManager = LinearLayoutManager(context)

        fileRecyclerViewAdapter = FileRecyclerViewAdapter({ abstractFile, position ->
        }, {}, {
            abstractFile, position ->
        })

        fileRecyclerViewAdapter.currentOrientation = ORIENTATION_LIST_TYPE

        searchedFileRecyclerView.adapter = fileRecyclerViewAdapter

    }


    private fun handleSelectedSearchTypeItemCountOrInputTextChanged() {

        if (currentInputText.isEmpty() && selectedSearchTypeItems.isEmpty()) {

            toolbarViewModel.showMenu.set(false)

            loadingViewModel.showLoading.set(false)
            noContentViewModel.showNoContent.set(false)

            selectedSearchTypeItemRecyclerView.visibility = View.GONE
            fileTypeTitleTv.visibility = View.VISIBLE

            searchedFileRecyclerView.visibility = View.INVISIBLE

            searchTypeItemRecyclerView.visibility = View.VISIBLE

        } else {

            fileTypeTitleTv.visibility = View.GONE

            searchTypeItemRecyclerView.visibility = View.INVISIBLE

            if (currentInputText.isNotEmpty())
                toolbarViewModel.showMenu.set(true)

            if (selectedSearchTypeItems.isNotEmpty()) {

                selectedSearchTypeItemRecyclerView.visibility = View.VISIBLE

                searchTypeCardViewAdapter.setItemList(selectedSearchTypeItems)
                searchTypeCardViewAdapter.notifyDataSetChanged()

            } else {

                selectedSearchTypeItemRecyclerView.visibility = View.GONE

            }

            searchedFileRecyclerView.visibility = View.VISIBLE

            startSearch()

        }

    }

    private fun startSearch() {

        loadingViewModel.showLoading.set(true)

        val searchKeys = mutableListOf<String>()

        if (currentInputText.isNotEmpty())
            searchKeys.add(currentInputText)

        selectedSearchTypeItems.forEach {

            when (it.imageResID) {
                R.drawable.photo_image -> searchKeys.add(".jpg")
                R.drawable.word -> searchKeys.add(".docx")
                R.drawable.pdf -> searchKeys.add(".pdf")
                R.drawable.power_point -> searchKeys.add(".ppt")
                R.drawable.excel -> searchKeys.add(".xlsx")
            }

        }

        fileDataSource.searchFile(searchKeys, object : BaseLoadDataCallback<AbstractRemoteFile> {

            override fun onSucceed(data: MutableList<AbstractRemoteFile>?, operationResult: OperationResult?) {

                if (data!!.isEmpty()) {

                    loadingViewModel.showLoading.set(false)
                    noContentViewModel.showNoContent.set(true)

                } else {

                    loadingViewModel.showLoading.set(false)
                    noContentViewModel.showNoContent.set(false)

                    handleSearchSucceed(data)

                }

            }

            override fun onFail(operationResult: OperationResult?) {
                loadingViewModel.showLoading.set(false)
                noContentViewModel.showNoContent.set(true)
            }

        })

    }

    private fun handleSearchSucceed(abstractFiles: MutableList<AbstractRemoteFile>) {

        val folderViewItems = mutableListOf<ViewItem>()
        val fileViewItems = mutableListOf<ViewItem>()

        abstractFiles.forEach {

            if (it.isFolder) {

                val folderItemViewModel = FolderItemViewModel(it as RemoteFolder)
                folderItemViewModel.showOfflineAvailableIv.set(true)
                folderItemViewModel.showMoreBtn.set(false)

                folderViewItems.add(ItemFolder(folderItemViewModel))
            } else {

                val fileItemViewModel = FileItemViewModel(it as RemoteFile)
                fileItemViewModel.showOfflineAvailableIv.set(true)
                fileItemViewModel.showMoreBtn.set(false)

                fileViewItems.add(ItemFile(fileItemViewModel))
            }

        }

        val viewItems = mutableListOf<ViewItem>()

        viewItems.addAll(folderViewItems)
        viewItems.addAll(fileViewItems)

        fileRecyclerViewAdapter.setItemList(viewItems)
        fileRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun showSearchTypeItemRecyclerView() {

        toolbarViewModel.showMenu.set(false)

        editText.text.clear()

        handleSelectedSearchTypeItemCountOrInputTextChanged()

    }

}


data class SearchTypeItem(val imageResID: Int, val text: String)

class SearchTypeAdapter(val itemOnClick: (searchTypeItem: SearchTypeItem) -> Unit) : BaseRecyclerViewAdapter<SimpleViewHolder, SearchTypeItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val view = parent?.inflateView(R.layout.search_type_item)

        return SimpleViewHolder(view)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val view = holder?.itemView

        val searchTypeItem = mItemList[position]

        view?.typeImageView?.setImageResource(searchTypeItem.imageResID)
        view?.typeTextView?.text = searchTypeItem.text

        view?.setOnClickListener {
            itemOnClick(searchTypeItem)
        }

    }

}

class SearchTypeCardViewAdpater(val deleteTypeTvOnClick: (searchTypeItem: SearchTypeItem) -> Unit) : BaseRecyclerViewAdapter<SimpleViewHolder, SearchTypeItem>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

        val view = parent?.inflateView(R.layout.search_type_card)

        return SimpleViewHolder(view)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

        val view = holder?.itemView

        val searchTypeItem = mItemList[position]

        view?.cardTypeImageView?.setImageResource(searchTypeItem.imageResID)
        view?.cardTypeTextView?.text = searchTypeItem.text

        view?.deleteTypeIv?.setOnClickListener {
            deleteTypeTvOnClick(searchTypeItem)
        }


    }

}