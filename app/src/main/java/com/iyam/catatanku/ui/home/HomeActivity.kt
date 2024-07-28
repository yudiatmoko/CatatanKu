package com.iyam.catatanku.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iyam.catatanku.R
import com.iyam.catatanku.ads.AdsManagerImpl
import com.iyam.catatanku.data.FileCallback
import com.iyam.catatanku.data.NoteRepositoryImpl
import com.iyam.catatanku.databinding.ActivityHomeBinding
import com.iyam.catatanku.ui.detail.NoteDetailActivity

class HomeActivity : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(
            layoutInflater,
            window.decorView.findViewById(android.R.id.content),
            false
        )
    }
    private lateinit var listAdapter: SimpleAdapter
    private val dataList: ArrayList<Map<String, Any>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initAdapter()
        setListView()
        setOnClickListeners()
        setupAdManager()
    }

    private fun initAdapter() {
        listAdapter = SimpleAdapter(
            this@HomeActivity,
            dataList,
            android.R.layout.simple_list_item_2,
            arrayOf(NAME, DATE),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        binding.lvNotes.adapter = listAdapter
    }

    private fun setupAdManager() {
        AdsManagerImpl(this).apply {
            initAd()
            showBannerAd(binding.llAdsContainer)
        }
    }

    private fun setOnClickListeners() {
        binding.lvNotes.setOnItemLongClickListener { adapterView, _, i, _ ->
            val data = adapterView.adapter.getItem(i) as Map<*, *>
            showDeleteDialog(data[NAME].toString(), i)
        }
        binding.lvNotes.setOnItemClickListener { adapterView, _, i, _ ->
            val data = adapterView.adapter.getItem(i) as Map<*, *>
            val fileName = data[NAME].toString()
            val fileContent = NoteRepositoryImpl(this).readFile(fileName)
            NoteDetailActivity.startActivityUpdate(this, fileName, fileContent)
        }
    }

    private fun showDeleteDialog(fileName: String, position: Int): Boolean {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_file_title)
            .setMessage(getString(R.string.are_you_sure_to_delete_this_file, fileName))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.yes_text) { _, _ ->
                NoteRepositoryImpl(this@HomeActivity).deleteFile(fileName, object : FileCallback {
                    override fun onSuccess() {
                        dataList.removeAt(position)
                        listAdapter.notifyDataSetChanged()
                        if (dataList.isEmpty()) setListView()
                        showToast(getString(R.string.file_deleted))
                    }

                    override fun onError(errorMessage: String) {
                        showToast(errorMessage)
                    }
                })
            }
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show()
        return true
    }

    private fun setListView() {
        val repo = NoteRepositoryImpl(this)
        dataList.clear()
        repo.getFiles(dataList, object : FileCallback {
            override fun onSuccess() {
                listAdapter.notifyDataSetChanged()
            }

            override fun onError(errorMessage: String) {
                showToast(errorMessage)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_notes, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_notes) navigateToDetail()
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToDetail() {
        NoteDetailActivity.startActivity(this)
    }

    override fun onResume() {
        super.onResume()
        setListView()
    }

    companion object {
        const val NAME = "name"
        const val DATE = "date"
    }
}